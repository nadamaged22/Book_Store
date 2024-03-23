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
                if (request.startsWith("ADD_BOOK")) {
                    handleAddBookRequest(request); // Pass the reader to handleAddBookRequest
                }
                if (request.startsWith("SEE_BOOKS")) {
                    handleSeeBooksRequest(); // Pass the reader to handleAddBookRequest
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

}
















