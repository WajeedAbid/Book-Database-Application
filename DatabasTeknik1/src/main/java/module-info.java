module se.kth.wajeed.labb3.databasteknik1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;

    opens se.kth.wajeed.labb3.databasteknik1 to javafx.fxml;
    exports se.kth.wajeed.labb3.databasteknik1;
}
