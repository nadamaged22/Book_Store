package DBConnection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DB {
    private static  MongoClient client;
    private static  MongoDatabase db;
    public static void initializeDatabaseConnection() {
        client = MongoClients.create("mongodb+srv://nadamaged:nnn123nnn@atlascluster.jgnsmsu.mongodb.net/?retryWrites=true&w=majority&appName=AtlasCluster");
        // Access the database
        db = client.getDatabase("BookStore");
    }

    public static MongoCollection<Document> getUserCollection(){
        return db.getCollection("User");
    }
    //to get book collection
    public static MongoCollection<Document> getBookCollection(){
        return db.getCollection("Book");
    }
    public static MongoCollection<Document> getRequestCollection(){
        return db.getCollection("Request");
    }

    public static MongoCollection<Document> getSequenceCollection(){
        return db.getCollection("sequences");
    }

}
