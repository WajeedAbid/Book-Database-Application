package se.kth.wajeed.labb3.databasteknik1.model;

import java.util.ArrayList;
import java.util.List;

public class Book {

    private final int bookId;
    private final String title;
    private final String publisher;
    private final String isbn;

    private Integer rating; // kan vara null
    private final List<String> genres;

    private final List<Author> authors;

    // Gamla konstruktor-signaturen (bakåtkompatibel)
    public Book(int bookId, String title, String publisher, String isbn) {
        this(bookId, title, publisher, isbn, null);
    }

    // Nya konstruktor-signaturen (för MySqlBooksDb / nya filer)
    public Book(int bookId, String title, String publisher, String isbn, Integer rating) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title is null/blank");
        }
        this.bookId = bookId;
        this.title = title;
        this.publisher = publisher;
        this.isbn = isbn;
        this.rating = rating;

        this.authors = new ArrayList<>();
        this.genres = new ArrayList<>();
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getRating() {
        return rating;
    }

    public String getRatingText() {
        return rating == null ? "" : String.valueOf(rating);
    }

    public List<String> getGenres() {
        return genres;
    }

    public void addGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) return;
        genres.add(genre);
    }

    public void clearGenres() {
        genres.clear();
    }

    public String getGenresText() {
        if (genres.isEmpty()) return "";
        return String.join(", ", genres);
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void addAuthor(Author author) {
        if (author == null) {
            throw new IllegalArgumentException("author is null");
        }
        authors.add(author);
    }

    @Override
    public String toString() {
        return title;
    }
}
