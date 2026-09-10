package se.kth.wajeed.labb3.databasteknik1.model;
import java.util.List;

public class BooksService {

    private final BooksDb db;

    public BooksService(BooksDb db) {
        this.db = db;
    }

    public void start() throws DbExceptions {
        db.connect();
    }

    public void stop() throws DbExceptions {
        db.disconnect();
    }

    public List<Book> searchTitle(String title) throws DbExceptions {
        return db.searchByTitle(title);
    }

    public List<Book> searchIsbn(String isbn) throws DbExceptions {
        return db.searchByIsbn(isbn);
    }

    public List<Book> searchAuthor(String author) throws DbExceptions {
        return db.searchByAuthor(author);
    }

    public Book bookWithAuthors(int bookId) throws DbExceptions {
        return db.getBookWithAuthors(bookId);
    }
}
