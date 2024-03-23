package BookClient;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class BookClient {
    private int port;
    private String host;

    public BookClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void startClient() {
        try (Socket socket = new Socket(host, port)) {
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Scanner scanner = new Scanner(System.in);

            while (true) {
                displayMenu();
                String choice = scanner.nextLine();
                switch (choice) {
                    case "1":
                        seeAvailableBooks(writer, serverReader);
                        break;
                    case "2":
                        addBook(writer, scanner);
                        break;
                    case "exit":
                        return; // Exit the method and close the socket
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error communicating with server: " + e.getMessage());
        }
    }

    private void displayMenu() {
        System.out.println("Menu:");
        System.out.println("1. See available books");
        System.out.println("2. Add a book");
        System.out.println("Enter your choice (Type 'exit' to finish): ");
    }

    private void seeAvailableBooks(BufferedWriter writer, BufferedReader serverReader) throws IOException {
        writer.write("SEE_BOOKS");
        writer.newLine();
        writer.flush();
        String response;
        System.out.println("Available books:");
        while ((response = serverReader.readLine()) != null) {
            if (response.equals("END_OF_BOOKS")) {
                break; // Stop reading when encountering the delimiter
            }
            System.out.println(response);
        }
    }


    private void addBook(BufferedWriter writer, Scanner scanner) throws IOException {

        System.out.println("Enter book details:");
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Author: ");
        String author = scanner.nextLine();
        System.out.print("Genre: ");
        String genre = scanner.nextLine();
        double price = 0;
        boolean validPrice = false;
        while (!validPrice) {
            try {
                System.out.print("Price: ");
                String priceInput = scanner.nextLine();
                // Validate input to ensure it contains only one decimal point
                if (priceInput.chars().filter(ch -> ch == '.').count() == 1) {
                    price = Double.parseDouble(priceInput);
                    validPrice = true;
                } else {
                    System.out.println("Invalid input. Please enter a valid price.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid price.");
            }
        }
        int quantity = 0;
        boolean validQuantity = false;
        while (!validQuantity) {
            try {
                System.out.print("Quantity: ");
                quantity = Integer.parseInt(scanner.nextLine());
                validQuantity = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid quantity.");
            }
        }
        // Send request to server to add the book
        writer.write("ADD_BOOK," + title + "," + author + "," + genre + "," + price + "," + quantity);
        writer.newLine();
        writer.flush();
    }

    public static void main(String[] args) {
        BookClient bookClient = new BookClient("localhost", 6666);
        bookClient.startClient();
    }
}
