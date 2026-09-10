package se.kth.wajeed.labb3.databasteknik1.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import se.kth.wajeed.labb3.databasteknik1.model.Author;
import se.kth.wajeed.labb3.databasteknik1.model.Book;
import se.kth.wajeed.labb3.databasteknik1.model.BooksDb;
import se.kth.wajeed.labb3.databasteknik1.model.DbExceptions;
import se.kth.wajeed.labb3.databasteknik1.view.BooksView;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.List;

public class BooksController {

    private final BooksDb db;
    private final BooksView view;

    public BooksController(BooksDb db, BooksView view) {
        if (db == null || view == null) {
            throw new IllegalArgumentException("db/view is null");
        }
        this.db = db;
        this.view = view;
        hookEvents();
    }

    public void start() {
        view.setStatus("Ansluter...");

        runDbTask(
                () -> db.connect(),
                () -> view.setStatus("Ansluten")
        );
    }

    public void stop() {
        runDbTask(
                () -> db.disconnect(),
                () -> view.setStatus("Frånkopplad")
        );
    }

    private void hookEvents() {

        view.getShowAuthorsButton().setDisable(true);
        view.getRateButton().setDisable(true);


        view.getTable().getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean disabled = (newV == null);
            view.getShowAuthorsButton().setDisable(disabled);
            view.getRateButton().setDisable(disabled);
        });

        view.getSearchButton().setOnAction(e -> onSearch());
        view.getShowAuthorsButton().setOnAction(e -> onShowAuthors());
        view.getRateButton().setOnAction(e -> onRateBook());
        view.getAddBookButton().setOnAction(e -> onAddBook());

    }


    private void onSearch() {
        String text = view.getSearchField().getText();
        if (text == null) text = "";

        BooksView.SearchMode mode = view.getSearchModeChoice().getValue();
        if (mode == null) mode = BooksView.SearchMode.TITLE;

        view.setStatus("Söker...");

        String finalText = text;
        BooksView.SearchMode finalMode = mode;

        runDbTaskBooks(
                () -> {
                    if (finalMode == BooksView.SearchMode.ISBN) {
                        return db.searchByIsbn(finalText);
                    } else if (finalMode == BooksView.SearchMode.AUTHOR) {
                        return db.searchByAuthor(finalText);
                    } else if (finalMode == BooksView.SearchMode.GENRE) {
                        return db.searchByGenre(finalText);
                    } else if (finalMode == BooksView.SearchMode.RATING) {
                        int r;
                        try {
                            r = Integer.parseInt(finalText.trim());
                        } catch (NumberFormatException ex) {
                            throw new DbExceptions("Betyg måste vara ett heltal (t.ex. 1-5).");
                        }
                        return db.searchByRating(r);
                    } else {
                        return db.searchByTitle(finalText);
                    }
                },
                result -> {
                    view.setBooks(result);
                    view.setStatus("Hittade: " + result.size());
                }
        );
    }


    private void onShowAuthors() {
        Book selected = view.getTable().getSelectionModel().getSelectedItem();
        if (selected == null) return;

        view.setStatus("Hämtar författare...");

        runDbTaskBook(
                () -> db.getBookWithAuthors(selected.getBookId()),
                bookWithAuthors -> {
                    if (bookWithAuthors == null) {
                        view.setStatus("Boken hittades inte längre");
                        return;
                    }

                    List<Author> authors = bookWithAuthors.getAuthors();
                    StringBuilder sb = new StringBuilder();

                    if (authors.isEmpty()) {
                        sb.append("Inga författare kopplade.");
                    } else {
                        for (Author a : authors) {
                            sb.append("• ").append(a.getFullName()).append("\n");
                        }
                    }

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
                    alert.setHeaderText("Författare: " + bookWithAuthors.getTitle());
                    alert.setTitle("Författare");
                    alert.showAndWait();

                    view.setStatus("Klar");
                }
        );
    }

    // ---- Tråd-hjälpare (DB i separat tråd) ----

    private void runDbTask(DbRunnable work, Runnable onSuccessUi) {
        Thread t = new Thread(() -> {
            try {
                work.run();
                Platform.runLater(onSuccessUi);
            } catch (DbExceptions ex) {
                Platform.runLater(() -> showError(ex));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void runDbTaskBooks(DbBooksSupplier work, UiBooksConsumer onSuccessUi) {
        Thread t = new Thread(() -> {
            try {
                List<Book> result = work.get();
                Platform.runLater(() -> onSuccessUi.accept(result));
            } catch (DbExceptions ex) {
                Platform.runLater(() -> showError(ex));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void runDbTaskBook(DbBookSupplier work, UiBookConsumer onSuccessUi) {
        Thread t = new Thread(() -> {
            try {
                Book result = work.get();
                Platform.runLater(() -> onSuccessUi.accept(result));
            } catch (DbExceptions ex) {
                Platform.runLater(() -> showError(ex));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void runDbTaskBookId(DbIntSupplier work, UiIntConsumer onSuccessUi) {
        Thread t = new Thread(() -> {
            try {
                int id = work.get();
                Platform.runLater(() -> onSuccessUi.accept(id));
            } catch (DbExceptions ex) {
                Platform.runLater(() -> showError(ex));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FunctionalInterface
    private interface DbIntSupplier {
        int get() throws DbExceptions;
    }

    @FunctionalInterface
    private interface UiIntConsumer {
        void accept(int value);
    }


    private void showError(Exception ex) {
        view.setStatus("Fel");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Databasfel");
        alert.setHeaderText("Något gick fel");
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }

    private void onRateBook() {
        Book selected = view.getTable().getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Dialog med val 1-5
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(3, 1, 2, 3, 4, 5);
        dialog.setTitle("Sätt betyg");
        dialog.setHeaderText("Betyg för: " + selected.getTitle());
        dialog.setContentText("Välj betyg (1-5):");

        dialog.showAndWait().ifPresent(rating -> {

            view.setStatus("Sätter betyg...");

            int bookId = selected.getBookId();

            runDbTask(
                    () -> db.rateBook(bookId, rating),
                    () -> {
                        // uppdatera objektet i tabellen
                        selected.setRating(rating);
                        view.getTable().refresh();

                        view.setStatus("Betyg sparat");
                    }
            );
        });
    }

    private void onAddBook() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Lägg till bok");
        dialog.setHeaderText("Fyll i uppgifter (författare/genre kan vara flera, separera med komma).");

        ButtonType ok = new ButtonType("Lägg till", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField titleField = new TextField();
        TextField publisherField = new TextField();
        TextField isbnField = new TextField();
        TextField authorsField = new TextField();
        TextField genresField = new TextField();

        titleField.setPromptText("Titel");
        publisherField.setPromptText("Förlag");
        isbnField.setPromptText("ISBN");
        authorsField.setPromptText("t.ex. Tolkien, Another Author");
        genresField.setPromptText("t.ex. Fantasy, Sci-Fi");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Titel:"), titleField);
        grid.addRow(1, new Label("Förlag:"), publisherField);
        grid.addRow(2, new Label("ISBN:"), isbnField);
        grid.addRow(3, new Label("Författare:"), authorsField);
        grid.addRow(4, new Label("Genre:"), genresField);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ok) return;

            String title = titleField.getText();
            String publisher = publisherField.getText();
            String isbn = isbnField.getText();

            List<String> authors = splitCsv(authorsField.getText());
            List<String> genres = splitCsv(genresField.getText());

            view.setStatus("Lägger till bok...");

            runDbTaskBookId(
                    () -> db.addBook(title, publisher, isbn, authors, genres),
                    newBookId -> {

                        Book b = new Book(newBookId, title, publisher, isbn, null);
                        for (String g : genres) b.addGenre(g);

                        view.getTable().getItems().add(b);
                        view.setStatus("Bok tillagd");
                    }
            );
        });
    }

    // Hjälp: dela komma-separerad text till lista
    private List<String> splitCsv(String text) {
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        if (text == null) return list;

        for (String part : text.split(",")) {
            String s = part.trim();
            if (!s.isEmpty()) list.add(s);
        }
        return list;
    }




    @FunctionalInterface
    private interface DbRunnable {
        void run() throws DbExceptions;
    }

    @FunctionalInterface
    private interface DbBooksSupplier {
        List<Book> get() throws DbExceptions;
    }

    @FunctionalInterface
    private interface DbBookSupplier {
        Book get() throws DbExceptions;
    }

    @FunctionalInterface
    private interface UiBooksConsumer {
        void accept(List<Book> value);
    }

    @FunctionalInterface
    private interface UiBookConsumer {
        void accept(Book value);
    }
}
