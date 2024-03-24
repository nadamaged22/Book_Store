package ClientHandler;

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

    public ClientHandler(Socket socket) {
        this.socket = socket;
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
                if (request.startsWith("SEE_BOOKS")) {
                    handleSeeBooksRequest(); // Pass the reader to handleAddBookRequest
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
                // Call the method to add the book to the database
                DBMethods.addBook(title, author, genre, price, quantity);
                // Notify the server that a book has been added
//                BookServer.notifyBookAdded("Book added: " + title);
            } else {
                System.out.println("Invalid request format: " + request);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Handle any errors gracefully
            System.out.println("Error handling add book request: " + e.getMessage());
        }
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
                    // Retrieve the numeric values
                    int bookId = bookDoc.getInteger("_id");
                    String title = bookDoc.getString("title");
                    String author = bookDoc.getString("author");
                    String genre = bookDoc.getString("genre");
                    // Retrieve quantity as Integer
                    int quantity = bookDoc.getInteger("quantity");
                    // Retrieve price as Double
                    double price = bookDoc.getDouble("price");
                    // Format the book details and send them to the client
                    String bookDetails = String.format("Book ID: %d%nTitle: %s%nAuthor: %s%nGenre: %s%nPrice: %.2f%nQuantity: %d%n",
                            bookId, title, author, genre, price, quantity);
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
















