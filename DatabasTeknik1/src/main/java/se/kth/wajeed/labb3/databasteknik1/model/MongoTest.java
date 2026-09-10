package se.kth.wajeed.labb3.databasteknik1.model;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoTest {
    public static void main(String[] args) {
        String uri = "mongodb://clientApp:AppPass123!@127.0.0.1:27017/booksdb?authSource=admin";
        try(MongoClient client = MongoClients.create(uri)){
            MongoDatabase database = client.getDatabase("booksdb");
            System.out.println("Connected OK. Collections:");
            for(String name : database.listCollectionNames()){
                System.out.println(" - " + name);
            }
        }
    }
}
