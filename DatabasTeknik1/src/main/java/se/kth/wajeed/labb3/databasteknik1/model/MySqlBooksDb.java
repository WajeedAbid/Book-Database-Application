package se.kth.wajeed.labb3.databasteknik1.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class MySqlBooksDb implements BooksDb {

    private static final String DB_NAME = "gruppuppgift2";
    private static final String URL =
            "jdbc:mysql://localhost:3306/" + DB_NAME +
                    "?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    private static final String USER = "books_client";
    private static final String PASS = "SXXXXe.XXXZ5";

    private Connection conn;

    @Override
    public void connect() throws DbExceptions {
        try {
            if (conn != null && !conn.isClosed()) {
                return;
            }
            conn = DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL connect failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void disconnect() throws DbExceptions {
        if (conn == null) return;
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL disconnect failed: " + ex.getMessage(), ex);
        } finally {
            conn = null;
        }
    }

    private void checkConnected() throws DbExceptions {
        try {
            if (conn == null || conn.isClosed()) {
                throw new DbExceptions("Not connected to database");
            }
        } catch (SQLException ex) {
            throw new DbExceptions("Not connected to database", ex);
        }
    }


    private Book mapBookRow(ResultSet rs) throws SQLException {
        int bookId = rs.getInt("BookID");
        String title = rs.getString("Title");
        String publisher = rs.getString("Publisher");
        String isbn = rs.getString("ISBN");

        Integer rating = (Integer) rs.getObject("Rating");
        Book b = new Book(bookId, title, publisher, isbn, rating);

        String genresCsv = rs.getString("Genres");
        if (genresCsv != null && !genresCsv.trim().isEmpty()) {
            // GROUP_CONCAT använder ", " i vår SQL
            String[] parts = genresCsv.split(",\\s*");
            for (String g : parts) b.addGenre(g);
        }
        return b;
    }

    @Override
    public List<Book> searchByTitle(String title) throws DbExceptions {
        checkConnected();

        String sql = """
                SELECT b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating,
                       GROUP_CONCAT(DISTINCT g.Name ORDER BY g.Name SEPARATOR ', ') AS Genres
                FROM t_book b
                LEFT JOIN t_bookgenre bg ON bg.BookID = b.BookID
                LEFT JOIN t_genre g ON g.GenreID = bg.GenreID
                WHERE b.Title LIKE ?
                GROUP BY b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating
                ORDER BY b.Title
                """;

        List<Book> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + (title == null ? "" : title) + "%";
            ps.setString(1, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapBookRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL searchByTitle failed: " + ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public List<Book> searchByIsbn(String isbn) throws DbExceptions {
        checkConnected();

        String sql = """
                SELECT b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating,
                       GROUP_CONCAT(DISTINCT g.Name ORDER BY g.Name SEPARATOR ', ') AS Genres
                FROM t_book b
                LEFT JOIN t_bookgenre bg ON bg.BookID = b.BookID
                LEFT JOIN t_genre g ON g.GenreID = bg.GenreID
                WHERE b.ISBN LIKE ?
                GROUP BY b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating
                ORDER BY b.Title
                """;

        List<Book> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + (isbn == null ? "" : isbn) + "%";
            ps.setString(1, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapBookRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL searchByIsbn failed: " + ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public List<Book> searchByAuthor(String authorName) throws DbExceptions {
        checkConnected();

        String sql = """
                SELECT b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating,
                       GROUP_CONCAT(DISTINCT g.Name ORDER BY g.Name SEPARATOR ', ') AS Genres
                FROM t_book b
                JOIN t_bookauthor ba ON ba.BookID = b.BookID
                JOIN t_author a ON a.AuthorID = ba.AuthorID
                LEFT JOIN t_bookgenre bg ON bg.BookID = b.BookID
                LEFT JOIN t_genre g ON g.GenreID = bg.GenreID
                WHERE a.Fullname LIKE ?
                GROUP BY b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating
                ORDER BY b.Title
                """;

        List<Book> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + (authorName == null ? "" : authorName) + "%";
            ps.setString(1, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapBookRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL searchByAuthor failed: " + ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public List<Book> searchByGenre(String genre) throws DbExceptions {
        checkConnected();

        String sql = """
                SELECT b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating,
                       GROUP_CONCAT(DISTINCT g.Name ORDER BY g.Name SEPARATOR ', ') AS Genres
                FROM t_book b
                JOIN t_bookgenre bg ON bg.BookID = b.BookID
                JOIN t_genre g ON g.GenreID = bg.GenreID
                WHERE g.Name LIKE ?
                GROUP BY b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating
                ORDER BY b.Title
                """;

        List<Book> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + (genre == null ? "" : genre) + "%";
            ps.setString(1, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapBookRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL searchByGenre failed: " + ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public List<Book> searchByRating(int rating) throws DbExceptions {
        checkConnected();

        String sql = """
                SELECT b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating,
                       GROUP_CONCAT(DISTINCT g.Name ORDER BY g.Name SEPARATOR ', ') AS Genres
                FROM t_book b
                LEFT JOIN t_bookgenre bg ON bg.BookID = b.BookID
                LEFT JOIN t_genre g ON g.GenreID = bg.GenreID
                WHERE b.Rating = ?
                GROUP BY b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating
                ORDER BY b.Title
                """;

        List<Book> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapBookRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL searchByRating failed: " + ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public Book getBookWithAuthors(int bookId) throws DbExceptions {
        checkConnected();

        String sql = """
                SELECT b.BookID, b.Title, b.Publisher, b.ISBN, b.Rating,
                       a.AuthorID, a.Fullname
                FROM t_book b
                LEFT JOIN t_bookauthor ba ON ba.BookID = b.BookID
                LEFT JOIN t_author a ON a.AuthorID = ba.AuthorID
                WHERE b.BookID = ?
                ORDER BY a.Fullname
                """;

        Book book = null;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (book == null) {
                        int id = rs.getInt("BookID");
                        String title = rs.getString("Title");
                        String publisher = rs.getString("Publisher");
                        String isbn = rs.getString("ISBN");
                        Integer rating = (Integer) rs.getObject("Rating");
                        book = new Book(id, title, publisher, isbn, rating);
                    }

                    Integer aId = (Integer) rs.getObject("AuthorID");
                    if (aId != null) {
                        String fullName = rs.getString("Fullname");
                        book.addAuthor(new Author(aId, fullName));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL getBookWithAuthors failed: " + ex.getMessage(), ex);
        }

        // Hämta genres i en extra query
        if (book != null) {
            try {
                loadGenres(book);
            } catch (SQLException ex) {
                throw new DbExceptions("MySQL loadGenres failed: " + ex.getMessage(), ex);
            }
        }

        return book;
    }

    private void loadGenres(Book book) throws SQLException {
        String sql = """
                SELECT g.Name
                FROM t_bookgenre bg
                JOIN t_genre g ON g.GenreID = bg.GenreID
                WHERE bg.BookID = ?
                ORDER BY g.Name
                """;

        book.clearGenres();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, book.getBookId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    book.addGenre(rs.getString("Name"));
                }
            }
        }
    }

    @Override
    public void rateBook(int bookId, int rating) throws DbExceptions {
        checkConnected();

        String sql = "UPDATE t_book SET Rating = ? WHERE BookID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setInt(2, bookId);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new DbExceptions("Ingen bok uppdaterades (BookID=" + bookId + ").");
            }
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL rateBook failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public int addBook(String title, String publisher, String isbn,
                       List<String> authors, List<String> genres) throws DbExceptions {

        checkConnected();

        if (title == null || title.trim().isEmpty()) {
            throw new DbExceptions("Titel får inte vara tom.");
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new DbExceptions("ISBN får inte vara tom.");
        }
        if (authors == null || authors.isEmpty()) {
            throw new DbExceptions("Minst en författare måste anges.");
        }
        if (genres == null || genres.isEmpty()) {
            throw new DbExceptions("Minst en genre måste anges.");
        }

        final boolean oldAuto;
        try {
            oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new DbExceptions("MySQL addBook failed to start transaction: " + ex.getMessage(), ex);
        }

        Integer createdBookId = null;
        DbExceptions toThrow = null;

        try {
            // 1) ISBN får inte redan finnas
            if (bookExistsByIsbn(isbn)) {
                throw new DbExceptions("ISBN finns redan: " + isbn);
            }

            // 2) Insert book
            createdBookId = insertBook(title, publisher, isbn);

            // 3) Koppla authors
            for (String a : authors) {
                int authorId = findOrCreateAuthorId(a);
                linkBookAuthor(createdBookId, authorId);
            }

            // 4) Koppla genres
            for (String g : genres) {
                int genreId = findOrCreateGenreId(g);
                linkBookGenre(createdBookId, genreId);
            }

            conn.commit();

        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException rb) {
                ex.addSuppressed(rb);
            }

            if (ex instanceof DbExceptions) {
                toThrow = (DbExceptions) ex;
            } else {
                toThrow = new DbExceptions("MySQL addBook failed: " + ex.getMessage(), ex);
            }

        } finally {
            try {
                conn.setAutoCommit(oldAuto);
            } catch (SQLException ex) {
                if (toThrow == null) {
                    toThrow = new DbExceptions("MySQL addBook failed to restore autoCommit: " + ex.getMessage(), ex);
                } else {
                    toThrow.addSuppressed(ex);
                }
            }
        }

        if (toThrow != null) {
            throw toThrow;
        }
        return createdBookId;
    }

// ---------- helpers ----------

    private boolean bookExistsByIsbn(String isbn) throws SQLException {
        String sql = "SELECT BookID FROM t_book WHERE ISBN = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int insertBook(String title, String publisher, String isbn) throws SQLException {
        String sql = "INSERT INTO t_book(Title, Publisher, ISBN, Rating) VALUES(?, ?, ?, NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title.trim());
            ps.setString(2, publisher == null ? null : publisher.trim());
            ps.setString(3, isbn.trim());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Kunde inte skapa bok (saknar genererat id).");
    }

    private int findOrCreateAuthorId(String fullName) throws SQLException {
        String name = (fullName == null) ? "" : fullName.trim();
        if (name.isEmpty()) {
            throw new SQLException("Författarnamn får inte vara tomt.");
        }

        // hitta
        String findSql = "SELECT AuthorID FROM t_author WHERE Fullname = ?";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("AuthorID");
            }
        }

        // skapa
        String insSql = "INSERT INTO t_author(Fullname) VALUES(?)";
        try (PreparedStatement ps = conn.prepareStatement(insSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Kunde inte skapa författare: " + name);
    }

    private int findOrCreateGenreId(String genreName) throws SQLException {
        String g = (genreName == null) ? "" : genreName.trim();
        if (g.isEmpty()) {
            throw new SQLException("Genre får inte vara tom.");
        }

        // hitta
        String findSql = "SELECT GenreID FROM t_genre WHERE Name = ?";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, g);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("GenreID");
            }
        }

        // skapa
        String insSql = "INSERT INTO t_genre(Name) VALUES(?)";
        try (PreparedStatement ps = conn.prepareStatement(insSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Kunde inte skapa genre: " + g);
    }

    private void linkBookAuthor(int bookId, int authorId) throws SQLException {
        String sql = "INSERT INTO t_bookauthor(BookID, AuthorID) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, authorId);
            ps.executeUpdate();
        }
    }

    private void linkBookGenre(int bookId, int genreId) throws SQLException {
        String sql = "INSERT INTO t_bookgenre(BookID, GenreID) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, genreId);
            ps.executeUpdate();
        }
    }



}
