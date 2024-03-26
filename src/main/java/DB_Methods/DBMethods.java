package DB_Methods;

import DBConnection.DB;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
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
                    .append("addedBy", addedBy)
                    .append("status", "Available");

            bookcol.insertOne(doc);
        }catch (Exception e) {
            System.err.println(e.toString());
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
    public static void addBorrowingRequest(String borrowerUsername, String lenderUsername,int BookID) {
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();
            int nextId = getNextSequence("request_sequence");
            Document doc = new Document("_id", nextId)
                    .append("borrowerUsername", borrowerUsername)
                    .append("lenderUsername", lenderUsername)
                    .append("status", "pending")
                    .append("BookID",BookID);
            requestCollection.insertOne(doc);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
    public static void updateBorrowingRequestStatus(int requestId, String status) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();
            Bson filter = eq("_id", requestId);
            Bson update = new Document("$set", new Document("status", status));
            requestCollection.updateOne(filter, update);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
    public static List<Document> getBorrowingRequestsForLender(String lenderUsername) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();
            List<Document> borrowingRequests = new ArrayList<>();

            // Create a query to find borrowing requests for the lender
            Bson query = Filters.eq("lenderUsername", lenderUsername);

            // Projection to include only specific fields
            Bson projection = Projections.fields(
                    Projections.include("borrowerUsername", "status", "BookID"));

            // Execute the query with projection and retrieve the documents
            FindIterable<Document> result = requestCollection.find(query).projection(projection);

            // Iterate through the documents
            for (Document doc : result) {
                // Create a new document with the desired fields only
                Document borrowingRequest = new Document()
                        .append("_id", doc.get("_id"))
                        .append("borrowerUsername", doc.get("borrowerUsername"))
                        .append("BookID", doc.get("BookID"))
                        .append("status", doc.get("status"));


                borrowingRequests.add(borrowingRequest);
            }

            return borrowingRequests;
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }
    public static String getLenderUsernameForBook(String bookId) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> bookcol = DB.getBookCollection();

            // Convert the bookId to integer (assuming it's stored as integer in the database)
            int bookIdInt = Integer.parseInt(bookId);

            // Create a query to find the book by its ID
            Bson query = Filters.eq("_id", bookIdInt);

            // Execute the query and retrieve the document
            Document bookDoc = bookcol.find(query).first();

            // Check if the document exists
            if (bookDoc != null) {
                // Retrieve the value of the "addedBy" field
                return bookDoc.getString("addedBy");
            } else {
                // Handle the case when the book with the provided ID is not found
                System.out.println("Book with ID " + bookId + " not found.");
            }
        } catch (NumberFormatException e) {
            // Handle the case when bookId cannot be parsed to integer
            System.out.println("Invalid book ID format: " + bookId);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        // Return null if there's any error or if the book is not found
        return null;
    }
    public static String getRequestId(String requestId) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();

            // Convert the requestId to integer (assuming it's stored as integer in the database)
            int requestIdInt = Integer.parseInt(requestId);

            // Create a query to find the request by its ID
            Bson query = Filters.eq("_id", requestIdInt);

            // Execute the query and retrieve the document
            Document requestDoc = requestCollection.find(query).first();

            // Check if the document exists
            if (requestDoc != null) {
                // Retrieve the value of the "_id" field
                return requestDoc.getObjectId("_id").toString();
            } else {
                // Handle the case when the request with the provided ID is not found
                System.out.println("Request with ID " + requestId + " not found.");
            }
        } catch (NumberFormatException e) {
            // Handle the case when requestId cannot be parsed to integer
            System.out.println("Invalid request ID format: " + requestId);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        // Return null if there's any error or if the request is not found
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
