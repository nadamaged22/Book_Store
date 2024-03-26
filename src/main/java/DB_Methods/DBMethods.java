package DB_Methods;

import DBConnection.DB;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
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
                    .append("addedBy", addedBy)
                    .append("status", "Available");

            bookcol.insertOne(doc);
        }catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public static boolean removeBook(int bookId, String currentUser) {
        DB.initializeDatabaseConnection();
        try {
            // Get the book Collection from DB
            MongoCollection<Document> bookcol = DB.getBookCollection();

            // Find the document with the specified _id
            Document bookDoc = bookcol.find(eq("_id", bookId)).first();
            if (bookDoc != null) {
                String addedBy = bookDoc.getString("addedBy");
                if (addedBy.equals(currentUser)) {
                    // Delete the document with the specified _id
                    DeleteResult result = bookcol.deleteOne(new Document("_id", bookId));
                    if (result.getDeletedCount() > 0) {
                        System.out.println("Book removed: " + bookId);
                        return true; // Book successfully removed
                    } else {
                        System.out.println("Book not found: " + bookId);
                        return false; // Book not found or not removed
                    }
                } else {
                    System.out.println("You do not have permission to remove this book.");
                    return false; // User does not have permission to remove this book
                }
            } else {
                System.out.println("Book not found: " + bookId);
                return false; // Book not found
            }
        } catch (Exception e) {
            System.err.println("Error removing book: " + e.getMessage());
            return false; // Exception occurred, book removal failed
        }
    }
    public static List<Document> showAvailableBooks() {
        try {
            MongoCollection<Document> bookcol = DB.getBookCollection();
            List<Document> bookList = new ArrayList<>();

            // Create a filter to retrieve only books with the status "Available"
            Bson filter = Filters.eq("status", "Available");

            // Find documents in the book collection that match the filter
            FindIterable<Document> books = bookcol.find(filter);

            // Iterate through the documents
            for (Document bookDoc : books) {
                bookList.add(bookDoc);
            }

            return bookList;
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
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
            if(status.equalsIgnoreCase("accepted"))
            {
                Document requestDoc = requestCollection.find(filter).first();
                assert requestDoc != null;
                int bookId = requestDoc.getInteger("BookID");
                updateBookStatusQauntity(bookId);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
    public static void updateBookStatusQauntity(int BookId) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> bookCollection = DB.getBookCollection();
            Bson filter = eq("_id", BookId);
            Document bookDoc = bookCollection.find(eq("_id",BookId)).first();

            int currentQuantity = bookDoc.getInteger("quantity");

            // Decrease the quantity by 1
            int newQuantity = Math.max(currentQuantity - 1, 0);
            bookCollection.updateOne(eq("_id",BookId),
                    Updates.set("quantity", newQuantity));

            // Update book status to "Unavailable" if quantity becomes 0
            if (newQuantity == 0) {
                bookCollection.updateOne(eq("_id",BookId),
                        Updates.set("status", "Unavailable"));
            }
            System.out.println("Book quantity and status updated successfully.");
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public static List<Document> getRequestsHistory(String borrowerUsername) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();
            List<Document> borrowingRequests = new ArrayList<>();

            // Create a query to find borrowing requests for the lender
            Bson query = Filters.eq("borrowerUsername", borrowerUsername);

            // Projection to include only specific fields
            Bson projection = Projections.fields(
                    Projections.include("lenderUsername", "status", "BookID"));

            // Execute the query with projection and retrieve the documents
            FindIterable<Document> result = requestCollection.find(query).projection(projection);

            // Iterate through the documents
            for (Document doc : result) {
                // Create a new document with the desired fields only
                Document borrowingRequest = new Document()
                        .append("_id", doc.get("_id"))
                        .append("lenderUsername", doc.get("lenderUsername"))
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
    public static List<Document> getPendingBorrowingRequestsForLender(String lenderUsername) {
        DB.initializeDatabaseConnection();
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();
            List<Document> pendingBorrowingRequests = new ArrayList<>();

            // Create a query to find pending borrowing requests for the lender
            Bson query = Filters.and(
                    Filters.eq("lenderUsername", lenderUsername),
                    Filters.eq("status", "pending")
            );

            // Projection to include only specific fields
            Bson projection = Projections.fields(
                    Projections.include("borrowerUsername", "status", "BookID")
            );

            // Execute the query with projection and retrieve the documents
            FindIterable<Document> result = requestCollection.find(query).projection(projection);

            // Iterate through the documents
            for (Document doc : result) {
                // Create a new document with the desired fields only
                Document pendingBorrowingRequest = new Document()
                        .append("_id", doc.get("_id"))
                        .append("borrowerUsername", doc.get("borrowerUsername"))
                        .append("BookID", doc.get("BookID"))
                        .append("status", doc.get("status"));

                pendingBorrowingRequests.add(pendingBorrowingRequest);
            }

            return pendingBorrowingRequests;
        } catch (Exception e) {
            System.err.println("Error retrieving pending borrowing requests for lender: " + e.getMessage());
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
    public static List<Document> getCurrentBorrowedBooks() {
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();
            // Create a query to find borrowing requests with status "borrowed"
            Bson query = Filters.eq("status", "Accepted");

            // Execute the query and retrieve the documents
            return requestCollection.find(query).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Error retrieving current borrowed books: " + e.getMessage());
            return null;
        }
    }
    public static List<Document> getAcceptedRequests() {
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();

            // Create a query to find borrowing requests with status "accepted"
            Bson query = Filters.eq("status", "Accepted");

            // Execute the query and retrieve the documents
            return requestCollection.find(query).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Error retrieving accepted requests: " + e.getMessage());
            return null;
        }
    }
    public static List<Document> getRejectedRequests() {
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();

            // Create a query to find borrowing requests with status "rejected"
            Bson query = Filters.eq("status", "rejected");

            // Execute the query and retrieve the documents
            return requestCollection.find(query).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Error retrieving rejected requests: " + e.getMessage());
            return null;
        }
    }
    public static List<Document> getPendingRequests() {
        try {
            MongoCollection<Document> requestCollection = DB.getRequestCollection();

            // Create a query to find borrowing requests with status "pending"
            Bson query = Filters.eq("status", "pending");

            // Execute the query and retrieve the documents
            return requestCollection.find(query).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Error retrieving pending requests: " + e.getMessage());
            return null;
        }
    }

public static List<Document> getAvailableBooks() {
    try {
        MongoCollection<Document> bookCollection = DB.getBookCollection();

        Bson query = Filters.eq("status", "Available");

        // Execute the query and retrieve the documents
        FindIterable<Document> availableBooks = bookCollection.find(query);

        // Convert the iterable to a list and return
        List<Document> availableBooksList = new ArrayList<>();
        for (Document book : availableBooks) {
            availableBooksList.add(book);
        }
        return availableBooksList;
    } catch (Exception e) {
        System.err.println("Error retrieving available books: " + e.getMessage());
        return null;
    }
}
    public static Document getLibraryStatusDocument() {
        try {
            Document libraryStatus = new Document();
            libraryStatus.append("Overall Library Status", true); // Placeholder for the boolean value

            // Add individual status fields to the document
            libraryStatus.append("Current Borrowed Books", DBMethods.getCurrentBorrowedBooks());
            libraryStatus.append("Available Books", DBMethods.getAvailableBooks());
            libraryStatus.append("Accepted Requests", DBMethods.getAcceptedRequests().size());
            libraryStatus.append("Rejected Requests", DBMethods.getRejectedRequests().size());
            libraryStatus.append("Pending Requests", DBMethods.getPendingRequests().size());

            return libraryStatus;
        } catch (Exception e) {
            System.err.println("Error retrieving library status: " + e.getMessage());
            return null;
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
    public static String getUserRole(String username) {
        DB.initializeDatabaseConnection();
        try {

                MongoCollection<Document> collection = DB.getUserCollection();
                Document query = new Document("username", username);
                Document result = collection.find(query).first();
                if (result != null) {
                    return result.getString("role");
                } else {
                    System.out.println("User not found: " + username);
                    return null;
                }

        } catch (Exception e) {
            System.err.println("Error getting user role: " + e.getMessage());
            return null;
        }
    }

}
