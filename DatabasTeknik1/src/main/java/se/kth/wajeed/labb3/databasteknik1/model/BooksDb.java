package se.kth.wajeed.labb3.databasteknik1.model;
import java.util.List;

public interface BooksDb {
    void connect() throws DbExceptions;
    void disconnect() throws DbExceptions;
    void rateBook(int bookId, int rating) throws DbExceptions;
    int addBook(String title, String publisher, String isbn,
                List<String> authors, List<String> genres) throws DbExceptions;



    List<Book> searchByTitle(String title) throws DbExceptions;
    List<Book> searchByIsbn(String isbn) throws DbExceptions;
    List<Book> searchByAuthor(String authorName) throws DbExceptions;

    List<Book> searchByGenre(String genre) throws DbExceptions;
    List<Book> searchByRating(int rating) throws DbExceptions;

    Book getBookWithAuthors(int bookId) throws DbExceptions;
}
