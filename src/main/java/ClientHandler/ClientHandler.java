package ClientHandler;

import BookClient.BookClient;
import BookServer.BookServer;
import DB_Methods.DBMethods;
import org.bson.Document;

import java.awt.print.Book;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedWriter writer;
    public BookClient bookClient;

    public ClientHandler(Socket socket, BookClient bookClient) {
        this.socket = socket;
        this.bookClient=bookClient;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String request;
            while ((request = reader.readLine()) != null) {
                // Handle client request
                if(request.startsWith("REGISTER_USER")){
                    handleRegisterRequest(request);
                }
                if(request.startsWith("LOGIN_USER")){
                    handleLoginRequest(request);
                }
                if (request.startsWith("ADD_BOOK")) {
                    handleAddBookRequest(request); // Pass the reader to handleAddBookRequest
                }
                if (request.startsWith("REMOVE_BOOK")) {
                    handleRemoveBookRequest(request);
                }
                if (request.startsWith("SEE_BOOKS")) {
                    handleSeeBooksRequest(); // Pass the reader to handleAddBookRequest
                }
                if (request.startsWith("SEARCH_BOOKS")) {
                   handleSearchBooksRequest(request); // Pass the reader to handleAddBookRequest
                }
                if (request.startsWith("BORROW_BOOK")) {
                    handleBorrowingRequest(request); // Pass the reader to handleAddBookRequest
                }
                if (request.startsWith("VIEW_BORROWING_REQUESTS")) {
                    handleRequest(request); // Pass the reader to handleAddBookRequest
                }
                if (request.startsWith("ACT_ON_REQUEST")) {
                    handleAcceptRejectRequest(request,writer); // Pass the reader to handleAddBookRequest
                }
                if (request.startsWith("SEE_USERS")) {
                    handleSeeUsersRequest(); // Pass the reader to handleAddBookRequest
                }

                // Add more handlers for other types of requests if needed
            }

            // Close resources
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException i) {
            System.out.println("Error Handling Client: " + i.getMessage());
        }
    }

private void handleRegisterRequest(String request) {
    try {
        // Parse the request and handle adding the user to the database
        String[] requestParts = request.split(",");
        // Check if requestParts contains enough elements
        if (requestParts.length >= 4) {
            // Extract user details from requestParts
            String name = requestParts[1];
            String username = requestParts[2];
            String password = requestParts[3];
            String role = requestParts[4];

            // Check if the username already exists
            if (DBMethods.usernameExist(username)) {
                System.out.println("Username already exists. Registration failed.");
                // Notify the current client about registration failure with reason
                BookServer.notifyRegisterResult(username, false, "Username already exists", this);
                return;
            }
            // Call the method to add the user to the database
            DBMethods.addUser(name, username, password, role);
            System.out.println("User registered successfully: " + name);
            bookClient.setCurrentUser(username);
            // Notify the current client about registration result
            BookServer.notifyRegisterResult(username, true, null, this); // Null for no specific reason
        } else {
            System.out.println("Invalid request format: " + request);
        }
    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
        // Handle any errors gracefully
        System.out.println("Error handling register user request: " + e.getMessage());
    }
}


    private void handleLoginRequest(String request) {
        try {
            String[] requestParts = request.split(",");
            // Check if requestParts contains enough elements
            if (requestParts.length >= 2) {
                String username = requestParts[1];
                String password = requestParts[2];

                // Assuming DBMethods has a method to check user credentials
                boolean loginSuccess = DBMethods.checkCredentials(username, password);
                if (loginSuccess) {
                    System.out.println("Login successful for user: " + username);
                    bookClient.setCurrentUser(username);
                    // Notify the current client about login success
                    BookServer.notifyLoginResult(username, 200, this); // 200 indicates success
                } else {
                    System.out.println("Invalid username or password");
                    // Determine the appropriate status code based on the failure reason
                    int statusCode = DBMethods.usernameExist(username) ? 401 : 404; // 401 for wrong password, 404 for username not found
                    // Notify the current client about login failure with appropriate status code
                    BookServer.notifyLoginResult(username, statusCode, this);
                }
            } else {
                System.out.println("Invalid request format: " + request);
                // Notify the current client about invalid request format
                BookServer.notifyLoginResult(null, 400, this); // 400 for bad request
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Handle any errors gracefully
            System.out.println("Error handling login request: " + e.getMessage());
            // Notify the current client about internal server error
            BookServer.notifyLoginResult(null, 500, this); // 500 for internal server error
        }
    }


    private void handleAddBookRequest(String request) {
        try {
            // Parse the request and handle adding the book to the database
            // Example: Split the request string and process the book details
            String[] requestParts = request.split(",");
            // Check if requestParts contains enough elements
            if (requestParts.length >= 6) {
                // Extract book details from requestParts
                String title = requestParts[1];
                String author = requestParts[2];
                String genre = requestParts[3];
                double price = Double.parseDouble(requestParts[4]);
                int quantity = Integer.parseInt(requestParts[5]);
                String currentuser = bookClient.getCurrentUser();
                // Call the method to add the book to the database
                DBMethods.addBook(title, author, genre, price, quantity,currentuser);
                // Notify the server that a book has been added
                BookServer.notifyBookAdded(title, this);
            } else {
                System.out.println("Invalid request format: " + request);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Handle any errors gracefully
            System.out.println("Error handling add book request: " + e.getMessage());
        }
    }

    private void handleRemoveBookRequest(String request) {
        try {
            // Check if the user is logged in
            String currentUser = bookClient.getCurrentUser();
            if (currentUser == null) {
                System.out.println("User not logged in.");
                return;
            }

            String[] requestParts = request.split(",");
            if (requestParts.length >= 2) {
                // Trim to remove leading/trailing spaces
                String title = requestParts[1].trim();

                // Call method from the DB Methods to remove the book
                boolean removed = DBMethods.removeBook(title, currentUser);

                // Notify the client about the outcome of the removal operation
                if (removed) {
                    // If the book was successfully removed, send a success message to the client
                    BookServer.notifyBookRemoved(title);
                } else {
                    // If the book removal failed, send a failure message to the client
                    // (You can adjust the message format as needed)
                    BookServer.notifyBookNotFound(title);

                    System.out.println("Failed to remove book: " + title);
                }
            } else {
                System.out.println("Invalid request format: " + request);
            }
        } catch (Exception e) {
            System.out.println("Error handling remove book request: " + e.getMessage());
        }
    }
    private String formatBookDetails(Document bookDoc) {
        int bookId = bookDoc.getInteger("_id");
        String title = bookDoc.getString("title");
        String author = bookDoc.getString("author");
        String genre = bookDoc.getString("genre");
        int quantity = bookDoc.getInteger("quantity");
        double price = bookDoc.getDouble("price");
        String addedBy = bookDoc.getString("addedBy");

        return String.format("Book ID: %d%nTitle: %s%nAuthor: %s%nGenre: %s%nPrice: %.2f%nQuantity: %d%nAdded By: %s%n",
                bookId, title, author, genre, price, quantity, addedBy);
    }

    private void handleSeeBooksRequest() {
        try {
            // Retrieve all books from the database
            List<Document> books = DBMethods.showAvailableBooks();
            // Check if there are any books in the database
            if (books.isEmpty()) {
                // Notify the client that there are no books available
                writer.write("No books available.");
                writer.newLine();
                writer.flush();
            } else {
                // Send the list of books to the client
                for (Document bookDoc : books) {
                    String bookDetails = formatBookDetails(bookDoc);
                    writer.write(bookDetails);
                    writer.newLine();
                    writer.flush();
                }
                // Add a delimiter to mark the end of the list of books
                writer.write("END_OF_BOOKS");
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            System.out.println("Error handling see books request: " + e.getMessage());
        }
    }
    private void handleSearchBooksRequest(String request) {
        try {
            // Parse the request and handle searching for books in the database
            String[] requestParts = request.split(",");
            // Check if requestParts contains enough elements
            if (requestParts.length == 3) {
                // Extract search keyword and field from requestParts
                String keyword = requestParts[1];
                String field = requestParts[2];

                // Retrieve books based on search criteria from the database
                List<Document> books = DBMethods.searchBooks(keyword, field);

                // Check if there are any books found
                if (books.isEmpty()) {
                    // Notify the client that no matching books were found
                    writer.write("END_OF_SEARCH");
                    writer.newLine();
                    writer.flush();
                } else {
                    // Send the list of matching books to the client
                    for (Document bookDoc : books) {
                        String bookDetails = formatBookDetails(bookDoc);
                        writer.write(bookDetails);
                        writer.newLine();
                        writer.flush();
                    }
                    // Add a delimiter to mark the end of the list of books
                    writer.write("END_OF_SEARCH");
                    writer.newLine();
                    writer.flush();
                }
            } else {
                System.out.println("Invalid request format: " + request);
            }
        } catch (IOException e) {
            System.out.println("Error handling search request: " + e.getMessage());
        }
    }
    private void handleBorrowingRequest(String request) {
        try {
            String[] requestParts = request.split(",");
            if (requestParts.length == 3) { // Check if there are three parts in the request
                String borrowingRequestType = requestParts[0];
                String bookId = requestParts[1]; // Extract bookId from requestParts[1]
                String lenderUsername = requestParts[2]; // Extract lenderUsername from requestParts[2]

                if (borrowingRequestType.equals("BORROW_BOOK")) {
                    // Retrieve borrowerUsername from the client
                    String borrowerUsername = bookClient.getCurrentUser();

                    // Add borrowing request to the database
                    DBMethods.addBorrowingRequest(borrowerUsername, lenderUsername, Integer.parseInt(bookId));

                    // Notify the client about successful request submission
//                    writer.write("REQUEST_SUBMITTED");
//                    writer.newLine();
//                    writer.flush();
                    BookServer.notifyBookBorrowed(bookId, this);
                } else {
                    System.out.println("Invalid borrowing request type: " + borrowingRequestType);
                }
            } else {
                System.out.println("Invalid request format: " + request);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error handling borrowing request: " + e.getMessage());
        }
    }
    private void handleRequest(String request) {
        try {
            String[] requestParts = request.split(",");
            if (requestParts.length == 1 && requestParts[0].equals("VIEW_BORROWING_REQUESTS")) {
                // Retrieve borrowing requests for the current user (Nada) from the database
                List<Document> requests = DBMethods.getBorrowingRequestsForLender(bookClient.getCurrentUser());

                // Send borrowing requests to the client
                for (Document req : requests) {
                    writer.write(req.toString()); // Assuming BorrowingRequest has a toString method
                    writer.newLine();
                }
                writer.write("END_OF_REQUESTS");
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            System.out.println("Error handling request: " + e.getMessage());
        }
    }
    private void handleAcceptRejectRequest(String request, BufferedWriter writer) {
        try {
            String[] requestParts = request.split(",");
            if (requestParts.length == 3 && requestParts[0].equals("ACT_ON_REQUEST")) {
                String requestId = requestParts[1];
                String action = requestParts[2];

                // Process the request action here
                boolean requestProcessed = processRequest(requestId, action);

                // Send response to client
                if (requestProcessed) {
                    writer.write("REQUEST_ACTION_PROCESSED");
                } else {
                    writer.write("REQUEST_ACTION_FAILED");
                }
                writer.flush();
            }
        } catch (IOException e) {
            System.out.println("Error handling accept/reject request: " + e.getMessage());
        }
    }
    private boolean processRequest(String requestId, String action) {
//        String validRequestId = DBMethods.getRequestId(requestId);
//        if (validRequestId == null) {
//            // Request ID is invalid or not found
//            return false;
//        }

        try {
            int parsedRequestId = Integer.parseInt(requestId);


            // Process the request action here
            if (action.equalsIgnoreCase("accept")) {
                DBMethods.updateBorrowingRequestStatus(parsedRequestId, "Accepted");
                return true;
            } else if (action.equalsIgnoreCase("reject")) {
              DBMethods.updateBorrowingRequestStatus(parsedRequestId, "Rejected");
                return true;
            } else {
                System.out.println("Invalid action provided.");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid request ID format.");
            return false;
        }
    }


    private void handleSeeUsersRequest() {
        try {
            // Retrieve all users from the database
            List<Document> users = DBMethods.getAllUsers();

            // Check if there are any users in the database
            if (users.isEmpty()) {
                // Notify the client that there are no users available
                writer.write("No users available.");
                writer.newLine();
                writer.flush();
            } else {
                // Send the list of users to the client
                for (Document userDoc : users) {
                    // Retrieve the user details
                    int userId = userDoc.getInteger("_id");
                    String name = userDoc.getString("name");
                    String username  = userDoc.getString("username");
                    String password  = userDoc.getString("password");
                    String role = userDoc.getString("role");

                    // Format the user details and send them to the client
                    String userDetails = String.format("User ID: %s%nName: %s%nUsername: %s%nPassword: %s%nRole: %s%n", userId,name,username,password,role);
                    writer.write(userDetails);
                    writer.newLine();
                    writer.flush();
                }
                // Add a delimiter to mark the end of the list of users
                writer.write("END_OF_USERS");
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            System.out.println("Error handling see users request: " + e.getMessage());
        }
    }


}
















