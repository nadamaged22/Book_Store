package DB_Methods;

import DBConnection.DB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

public class DBMethods {
    public static void addBook(String title,String author,String genre,double price,int quantity){
        MongoCollection<Document> bookcol= DB.getBookCollection();
        // Get the next sequence value
        int nextId = getNextSequence("bookstore_sequence");
        // Insert a document with the next sequence value as _id
        Document doc = new Document("_id", nextId)
                .append("title", title)
                .append("author", author)
                .append("genre", genre)
                .append("price", price)
                .append("quantity", quantity);

        bookcol.insertOne(doc);

    }
    public static void showAvailableBooks() {
        MongoCollection<Document> bookcol = DB.getBookCollection();

        // Find all documents in the book collection
        FindIterable<Document> books = bookcol.find();
        // Iterate through the documents
        MongoCursor<Document> iterator = books.iterator();
        while (iterator.hasNext()) {
            Document book = iterator.next();
            // Print the details of each book
            System.out.println("Book ID: " + book.getInteger("_id"));
            System.out.println("Title: " + book.getString("title"));
            System.out.println("Author: " + book.getString("author"));
            System.out.println("Genre: " + book.getString("genre"));
            System.out.println("Price: " + book.getDouble("price"));
            System.out.println("Quantity: " + book.getInteger("quantity"));
            System.out.println();
        }
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
