package se.kth.wajeed.labb3.databasteknik1;

import javafx.application.Application;
import se.kth.wajeed.labb3.databasteknik1.model.BooksDb;
import se.kth.wajeed.labb3.databasteknik1.model.MongoBooksDb;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(javafx.stage.Stage stage) {
        se.kth.wajeed.labb3.databasteknik1.model.BooksDb db =
                new se.kth.wajeed.labb3.databasteknik1.model.MongoBooksDb();


        se.kth.wajeed.labb3.databasteknik1.view.BooksView view =
                new se.kth.wajeed.labb3.databasteknik1.view.BooksView();

        se.kth.wajeed.labb3.databasteknik1.controller.BooksController controller =
                new se.kth.wajeed.labb3.databasteknik1.controller.BooksController(db, view);

        javafx.scene.Scene scene = new javafx.scene.Scene(view, 900, 600);
        stage.setTitle("Books");
        stage.setScene(scene);
        stage.show();

        controller.start();

        // viktigt enligt krav: stäng connection när appen stängs
        stage.setOnCloseRequest(e -> controller.stop());
    }
}
