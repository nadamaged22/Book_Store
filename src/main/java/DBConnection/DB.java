package DBConnection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DB {
    public static void main(String[] args){
        MongoClient client = MongoClients.create("mongodb+srv://nadamaged:nnn123nnn@atlascluster.jgnsmsu.mongodb.net/");
        // Access the database
        MongoDatabase db = client.getDatabase("BookStore");
        // Access collection
        MongoCollection<Document> bookcol = db.getCollection("Book");
        MongoCollection<Document> usercol= db.getCollection("User");

        // Get the next sequence value
        int nextId = getNextSequence(db, "bookstore_sequence");
        double price;
        int q;

        // Insert a document with the next sequence value as _id
        Document doc = new Document("_id", nextId).append("title", "have it all").append("author","nada maged").append("genre","fantasy").append("price",price=100.30).append("quantity",q=200);
        Document doc2=new Document("_id",nextId).append("name","nada").append("username","nada22").append("password","123");
        bookcol.insertOne(doc);
        usercol.insertOne(doc2);
    }

    private static int getNextSequence(MongoDatabase db, String sequenceName) {
        MongoCollection<Document> seqCollection = db.getCollection("sequences");

        Document filter = new Document("_id", sequenceName);
        Document update = new Document("$inc", new Document("seq", 1));

        Document sequence = seqCollection.findOneAndUpdate(filter, update);
        if (sequence == null) {
            seqCollection.insertOne(new Document("_id", sequenceName).append("seq", 1));
            return 1;
        } else {
            return sequence.getInteger("seq");
        }
    }
}
