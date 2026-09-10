package se.kth.wajeed.labb3.databasteknik1.model;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;


import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MongoBooksDb implements BooksDb {

    // ---- Connection settings ----
    private static final String DB_NAME = "booksdb";


    private static final String URI =
            "mongodb://clientApp:AppPass123!@127.0.0.1:27017/booksdb?authSource=admin";

    private static final String BOOKS_COL = "books";
    private static final String COUNTER_COL = "counter";

    private static final String SEQ_BOOK = "bookId";
    private static final String SEQ_AUTHOR = "authorId";

    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> books;
    private MongoCollection<Document> counter;

    @Override
    public void connect() throws DbExceptions {
        try {
            if (client != null) return;

            client = MongoClients.create(URI);
            db = client.getDatabase(DB_NAME);

            books = db.getCollection(BOOKS_COL);
            counter = db.getCollection(COUNTER_COL);

            ensureIndexes();

        } catch (Exception ex) {
            // städa om något gick fel
            try { disconnect(); } catch (DbExceptions ignore) {}
            throw new DbExceptions("Mongo connect failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void disconnect() throws DbExceptions {
        if (client != null) {
            client.close();
            client = null;
            db = null;
            books = null;
            counter = null;
        }
    }

    private void checkConnected() throws DbExceptions {
        if (client == null || db == null || books == null || counter == null) {
            throw new DbExceptions("Not connected to MongoDB");
        }
    }

    private void ensureIndexes() {

        books.createIndex(Indexes.ascending("bookId"), new IndexOptions().unique(true));


        books.createIndex(Indexes.ascending("isbn"), new IndexOptions().unique(true));


        books.createIndex(Indexes.ascending("title"));
        books.createIndex(Indexes.ascending("rating"));
        books.createIndex(Indexes.ascending("genres"));
        books.createIndex(Indexes.ascending("authors.fullname"));
    }

    // -------------------------
    // SÖK
    // -------------------------

    @Override
    public List<Book> searchByTitle(String title) throws DbExceptions {
        return searchRegex("title", title);
    }

    @Override
    public List<Book> searchByIsbn(String isbn) throws DbExceptions {
        return searchRegex("isbn", isbn);
    }

    @Override
    public List<Book> searchByAuthor(String authorName) throws DbExceptions {
        return searchRegex("authors.fullname", authorName);
    }

    @Override
    public List<Book> searchByGenre(String genre) throws DbExceptions {
        return searchRegex("genres", genre);
    }

    private List<Book> searchRegex(String field, String text) throws DbExceptions {
        checkConnected();
        try {
            String s = (text == null) ? "" : text.trim();


            var filter = s.isEmpty()
                    ? new Document()
                    : Filters.regex(field, Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE));

            List<Book> out = new ArrayList<>();
            for (Document d : books.find(filter).sort(Sorts.ascending("title"))) {
                out.add(toBook(d));
            }
            return out;

        } catch (Exception ex) {
            throw new DbExceptions("Mongo search failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Book> searchByRating(int rating) throws DbExceptions {
        checkConnected();
        try {
            List<Book> out = new ArrayList<>();
            for (Document d : books.find(eq("rating", rating)).sort(Sorts.ascending("title"))) {
                out.add(toBook(d));
            }
            return out;
        } catch (Exception ex) {
            throw new DbExceptions("Mongo searchByRating failed: " + ex.getMessage(), ex);
        }
    }

    // -------------------------
    // HÄMTA BOK + AUTHORS
    // -------------------------

    @Override
    public Book getBookWithAuthors(int bookId) throws DbExceptions {
        checkConnected();
        try {
            Document d = books.find(eq("bookId", bookId)).first();
            if (d == null) return null;
            return toBook(d);
        } catch (Exception ex) {
            throw new DbExceptions("Mongo getBookWithAuthors failed: " + ex.getMessage(), ex);
        }
    }

    // -------------------------
    // BETYGSÄTT
    // -------------------------

    @Override
    public void rateBook(int bookId, int rating) throws DbExceptions {
        checkConnected();
        try {
            //UpdateResult
            var res = books.updateOne(eq("bookId", bookId), set("rating", rating));
            if (res.getMatchedCount() == 0) {
                throw new DbExceptions("Ingen bok hittades (bookId=" + bookId + ")");
            }
        } catch (Exception ex) {
            throw new DbExceptions("Mongo rateBook failed: " + ex.getMessage(), ex);
        }
    }

    // -------------------------
    // LÄGG TILL BOK
    // -------------------------

    @Override
    public int addBook(String title, String publisher, String isbn,
                       List<String> authors, List<String> genres) throws DbExceptions {

        checkConnected();


        if (title == null || title.trim().isEmpty()) throw new DbExceptions("Titel får inte vara tom.");
        if (isbn == null || isbn.trim().isEmpty()) throw new DbExceptions("ISBN får inte vara tom.");
        if (authors == null || authors.isEmpty()) throw new DbExceptions("Minst en författare måste anges.");
        if (genres == null || genres.isEmpty()) throw new DbExceptions("Minst en genre måste anges.");

        String t = title.trim();
        String p = (publisher == null || publisher.trim().isEmpty()) ? null : publisher.trim();
        String i = isbn.trim();


        List<String> authorNames = new ArrayList<>();
        for (String a : authors) {
            if (a != null && !a.trim().isEmpty()) authorNames.add(a.trim());
        }
        if (authorNames.isEmpty()) throw new DbExceptions("Minst en författare måste anges.");

        List<String> genreNames = new ArrayList<>();
        for (String g : genres) {
            if (g != null && !g.trim().isEmpty()) genreNames.add(g.trim());
        }
        if (genreNames.isEmpty()) throw new DbExceptions("Minst en genre måste anges.");

        try {

            if (books.find(eq("isbn", i)).first() != null) {
                throw new DbExceptions("ISBN finns redan: " + i);
            }

            int newBookId = getNextSequence(SEQ_BOOK);

            List<Document> authorDocs = new ArrayList<>();
            for (String name : authorNames) {
                int newAuthorId = getNextSequence(SEQ_AUTHOR);
                authorDocs.add(new Document("authorId", newAuthorId)
                        .append("fullname", name));
            }

            Document doc = new Document("bookId", newBookId)
                    .append("title", t)
                    .append("publisher", p)
                    .append("isbn", i)
                    .append("rating", null)
                    .append("genres", genreNames)
                    .append("authors", authorDocs);

            books.insertOne(doc);
            return newBookId;

        } catch (MongoWriteException wex) {
            // Duplicate key (t.ex. isbn unique)
            if (wex.getError() != null && wex.getError().getCode() == 11000) {
                throw new DbExceptions("ISBN finns redan: " + isbn, wex);
            }
            throw new DbExceptions("Mongo addBook failed: " + wex.getMessage(), wex);

        } catch (DbExceptions ex) {
            throw ex;

        } catch (Exception ex) {
            throw new DbExceptions("Mongo addBook failed: " + ex.getMessage(), ex);
        }
    }

    // -------------------------
    // COUNTER
    // -------------------------

    /**
     * Hämtar nästa sekvensnummer atomiskt via counter-collection.
     * Vi använder dokument: { _id: "bookId", seq: 1 } (eller authorId).
     */
    private int getNextSequence(String key) throws DbExceptions {
        try {
            FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions()
                    .upsert(true)
                    .returnDocument(ReturnDocument.AFTER);

            // Enbart $inc (funkar även om dokumentet inte finns: då skapas det och seq blir 1)
            var update = inc("seq", 1);

            Document updated = counter.findOneAndUpdate(eq("_id", key), update, opt);

            if (updated == null) throw new DbExceptions("Counter update failed for " + key);

            Integer seq = updated.getInteger("seq");
            if (seq == null) throw new DbExceptions("Counter document saknar 'seq' för " + key);

            return seq;

        } catch (Exception ex) {
            throw new DbExceptions("Mongo counter failed: " + ex.getMessage(), ex);
        }
    }



    // -------------------------
    // MAPPING: Document -> Book
    // -------------------------

    private Book toBook(Document d) throws DbExceptions {
        Integer bookId = getIntAny(d, "bookId", "BookID");
        String title = getStringAny(d, "title", "Title");
        String publisher = getStringAny(d, "publisher", "Publisher");
        String isbn = getStringAny(d, "isbn", "ISBN");

        Integer rating = getIntAny(d, "rating", "Rating");

        if (bookId == null) throw new DbExceptions("Mongo document saknar bookId");
        if (title == null || title.trim().isEmpty()) throw new DbExceptions("Mongo document saknar title");

        Book b = new Book(bookId, title, publisher, isbn, rating);

        // genres: [String]
        List<String> genres = d.getList("genres", String.class);
        if (genres != null) {
            for (String g : genres) b.addGenre(g);
        }

        // authors: [{authorId:int, fullname:string}]
        List<Document> authDocs = d.getList("authors", Document.class);
        if (authDocs != null) {
            for (Document ad : authDocs) {
                Integer aid = getIntAny(ad, "authorId", "AuthorID");
                String fn = getStringAny(ad, "fullname", "fullName", "Fullname");

                if (aid != null && fn != null && !fn.trim().isEmpty()) {
                    b.addAuthor(new Author(aid, fn));
                }
            }
        }

        return b;
    }

    private Integer getIntAny(Document d, String... keys) {
        for (String k : keys) {
            Object o = d.get(k);
            if (o instanceof Integer) return (Integer) o;
            if (o instanceof Long) return ((Long) o).intValue();
        }
        return null;
    }

    private String getStringAny(Document d, String... keys) {
        for (String k : keys) {
            Object o = d.get(k);
            if (o instanceof String) return (String) o;
        }
        return null;
    }
}
