package ClientHandler;

import BookServer.BookServer;
import DB_Methods.DBMethods;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

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
                BookServer.notifyBookAdded("Book added: " + title);
            } else {
                System.out.println("Invalid request format: " + request);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Handle any errors gracefully
            System.out.println("Error handling add book request: " + e.getMessage());
        }
    }


}
