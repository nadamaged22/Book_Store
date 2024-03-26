package DB_Methods;

import DBConnection.DB;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DBMethods {

    public static void addUser(String name,String username,String password,String role){
        try{
            MongoCollection<Document>Usercol=DB.getUserCollection();
            int nextId=getNextSequence("USER_sequence");
            Document doc=new Document("_id",nextId)
                    .append("name",name)
                    .append("username",username)
                    .append("password",password)
                    .append("role",role);
            // Create a unique index on the username field
            IndexOptions indexOptions = new IndexOptions().unique(true);
            Usercol.createIndex(Indexes.ascending("username"), indexOptions);
            Usercol.insertOne(doc);
        }catch (Exception e){
            System.err.println(e.toString());
        }
    }
    public static boolean usernameExist(String name) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> collection = DB.getUserCollection();
            Document query = new Document("username", name); // Adjust the query to search for name
            FindIterable<Document> result = collection.find(query);
            return result.iterator().hasNext();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return false;
    }
    public static boolean checkCredentials(String username, String password) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> collection = DB.getUserCollection();
            Document query = new Document("username", username)
                    .append("password", password);
            FindIterable<Document> result = collection.find(query);
            return result.iterator().hasNext();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return false;
    }
    public static void addBook(String title,String author,String genre,double price,int quantity,String addedBy){
        try{
            MongoCollection<Document> bookcol= DB.getBookCollection();
            // Get the next sequence value
            int nextId = getNextSequence("bookstore_sequence");
            // Insert a document with the next sequence value as _id
            Document doc = new Document("_id", nextId)
                    .append("title", title)
                    .append("author", author)
                    .append("genre", genre)
                    .append("price", price)
                    .append("quantity", quantity)
                    .append("addedBy", addedBy);

            bookcol.insertOne(doc);
        }catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public static boolean removeBook(String title, String currentUser) {
        try {
            // Get the book Collection from DB
            MongoCollection<Document> bookcol = DB.getBookCollection();

            // Find the document with the specified title
            Document bookDoc = bookcol.find(eq("title", title)).first();
            if (bookDoc != null) {
                String addedBy = bookDoc.getString("addedBy");
                if (addedBy.equals(currentUser)) {
                    // Delete the document with the specified title
                    DeleteResult result = bookcol.deleteOne(new Document("title", title));
                    if (result.getDeletedCount() > 0) {
                        System.out.println("Book removed: " + title);
                        return true; // Book successfully removed
                    } else {
                        System.out.println("Book not found: " + title);
                        return false; // Book not found or not removed
                    }
                } else {
                    System.out.println("You do not have permission to remove this book.");
                    return false; // User does not have permission to remove this book
                }
            } else {
                System.out.println("Book not found: " + title);
                return false; // Book not found
            }
        } catch (Exception e) {
            System.err.println("Error removing book: " + e.getMessage());
            return false; // Exception occurred, book removal failed
        }
    }

    public static List<Document> showAvailableBooks() {
        try{
            MongoCollection<Document> bookcol = DB.getBookCollection();
            List<Document> bookList = new ArrayList<>();
            // Find all documents in the book collection
            FindIterable<Document> books = bookcol.find();
            // Iterate through the documents
            for (Document bookDoc : books) {
                bookList.add(bookDoc);
            }

            return bookList;
        }catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }
    public static List<Document> searchBooks(String keyword, String field) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> bookcol = DB.getBookCollection();
            List<Document> bookList = new ArrayList<>();

            // Create a query to search for books based on the specified field and keyword
            Bson searchQuery = Filters.regex(field, Pattern.compile(keyword, Pattern.CASE_INSENSITIVE));
            FindIterable<Document> books = bookcol.find(searchQuery);

            // Iterate through the documents
            for (Document bookDoc : books) {
                bookList.add(bookDoc);
            }
            return bookList;
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }
    public static List<Document> getAllUsers() {
        try{
            MongoCollection<Document> usercol = DB.getUserCollection();
            List<Document> userList = new ArrayList<>();
            // Find all documents in the user collection
            FindIterable<Document> users = usercol.find();
            // Iterate through the documents
            for (Document userDoc : users) {
                userList.add(userDoc);
            }
            return userList;
        }catch (Exception e) {
            System.err.println(e.toString());
        }

        return null;
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
