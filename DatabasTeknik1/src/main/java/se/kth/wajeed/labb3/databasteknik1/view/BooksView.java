package se.kth.wajeed.labb3.databasteknik1.view;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import se.kth.wajeed.labb3.databasteknik1.model.Book;

import java.util.List;

public class BooksView extends BorderPane {

    public enum SearchMode {
        TITLE("Titel"),
        ISBN("ISBN"),
        AUTHOR("Författare"),
        GENRE("Genre"),
        RATING("Betyg (1-5)");

        private final String label;

        SearchMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final TextField searchField = new TextField();
    private final ChoiceBox<SearchMode> searchModeChoice = new ChoiceBox<>();
    private final Button addBookButton = new Button("Lägg till bok");
    private final Button searchButton = new Button("Sök");
    private final Button showAuthorsButton = new Button("Visa författare");
    private final Button rateButton = new Button("Sätt betyg");
    private final Label statusLabel = new Label("Ej ansluten");

    private final TableView<Book> table = new TableView<>();
    private final ObservableList<Book> books = FXCollections.observableArrayList();

    public BooksView() {
        buildTopBar();
        buildTable();
        buildBottomBar();
        rateButton.setDisable(true);

        setPadding(new Insets(10));
        setCenter(table);

        table.setItems(books);

        searchModeChoice.getItems().addAll(SearchMode.values());
        searchModeChoice.setValue(SearchMode.TITLE);

        showAuthorsButton.setDisable(true);
    }

    private void buildTopBar() {
        searchField.setPromptText("Sök...");

        HBox top = new HBox(10, new Label("Sök på:"), searchModeChoice, searchField, searchButton);
        top.setPadding(new Insets(10));
        setTop(top);
    }

    private void buildTable() {
        TableColumn<Book, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getBookId()));

        TableColumn<Book, String> titleCol = new TableColumn<>("Titel");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Book, String> publisherCol = new TableColumn<>("Förlag");
        publisherCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPublisher() == null ? "" : data.getValue().getPublisher()
        ));

        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenresText()));

        TableColumn<Book, String> ratingCol = new TableColumn<>("Betyg");
        ratingCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRatingText()));

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getIsbn() == null ? "" : data.getValue().getIsbn()
        ));

        table.getColumns().addAll(idCol, titleCol, publisherCol, genreCol, ratingCol, isbnCol);

        idCol.setPrefWidth(70);
        titleCol.setPrefWidth(260);
        publisherCol.setPrefWidth(180);
        genreCol.setPrefWidth(220);
        ratingCol.setPrefWidth(80);
        isbnCol.setPrefWidth(220);
    }

    private void buildBottomBar() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottom = new HBox(10, statusLabel, spacer, addBookButton, rateButton, showAuthorsButton);

        bottom.setPadding(new Insets(10));
        setBottom(bottom);
    }

    public Button getRateButton() {
        return rateButton;
    }

    public Button getAddBookButton() {
        return addBookButton;
    }

    // --- API till Controller ---
    public TextField getSearchField() {
        return searchField;
    }

    public ChoiceBox<SearchMode> getSearchModeChoice() {
        return searchModeChoice;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public TableView<Book> getTable() {
        return table;
    }

    public Button getShowAuthorsButton() {
        return showAuthorsButton;
    }

    public void setBooks(List<Book> list) {
        books.setAll(list);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }
}
