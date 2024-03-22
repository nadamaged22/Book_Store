package DBConnection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DB {
    private static  MongoClient client;
    private static  MongoDatabase db;
    //intialize connection
// Initialize connection
    public static void initializeDatabaseConnection() {
        client = MongoClients.create("mongodb+srv://nadamaged:nnn123nnn@atlascluster.jgnsmsu.mongodb.net/");
        // Access the database
        db = client.getDatabase("BookStore");
    }

    //to get book collection
    public static MongoCollection<Document> getBookCollection(){
        return db.getCollection("Book");
    }
    //get user collection
    public static MongoCollection<Document> getUserCollection(){
        return db.getCollection("User");
    }
    public static MongoCollection<Document> getSequenceCollection(){
        return db.getCollection("sequences");
    }

}
