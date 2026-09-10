package se.kth.wajeed.labb3.databasteknik1.model;

import java.util.List;

public class DbTest {
    public static void main(String[] args) throws Exception {
        BooksDb db = new MySqlBooksDb();
        db.connect();

        List<Book> books = db.searchByTitle("Data");
        System.out.println("Hittade: " + books.size());
        for (Book b : books) {
            System.out.println(b.getBookId() + " - " + b.getTitle());
        }

        Book one = db.getBookWithAuthors(1);
        System.out.println("Bok 1: " + one.getTitle() + " authors=" + one.getAuthors());

        db.disconnect();
    }
}
