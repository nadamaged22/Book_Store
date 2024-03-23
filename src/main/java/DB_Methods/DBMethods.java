package DB_Methods;

import DBConnection.DB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class DBMethods {
    public static void addBook(String title,String author,String genre,double price,int quantity){
        MongoCollection<Document> bookcol= DB.getBookCollection();
        // Get the next sequence value
        int nextId = getNextSequence("bookstore_sequence");
//        String priceString = String.valueOf(price);
//
//        // Check if the price is a whole number (integer)
//        if (price == (long) price) {
//            // Append ".0" to the price
//            priceString += ".0";
//        }
        // Insert a document with the next sequence value as _id
        Document doc = new Document("_id", nextId)
                .append("title", title)
                .append("author", author)
                .append("genre", genre)
                .append("price", price)
                .append("quantity", quantity);

        bookcol.insertOne(doc);

    }
    public static List<Document> showAvailableBooks() {
        MongoCollection<Document> bookcol = DB.getBookCollection();
        List<Document> bookList = new ArrayList<>();
        // Find all documents in the book collection
        FindIterable<Document> books = bookcol.find();
        // Iterate through the documents
        for (Document bookDoc : books) {
            bookList.add(bookDoc);
        }

        return bookList;
    }

    private static int getNextSequence(String sequenceName) {
        MongoCollection<Document> seqCollection = DB.getSequenceCollection();

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
