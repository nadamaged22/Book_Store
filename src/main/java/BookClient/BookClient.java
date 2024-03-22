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
            BufferedWriter Writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader userReader = new BufferedReader(new InputStreamReader(System.in));
            Scanner scanner=new Scanner(System.in);

            // Thread for receiving messages from server
            Thread receiveThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = serverReader.readLine()) != null) {
                        System.out.println("Server response: " + response);
                    }
                } catch (IOException e) {
                    System.out.println("Error receiving message from server: " + e.getMessage());
                }
            });
            receiveThread.start();
            String choice;
            do {
                System.out.println("Menu:");
                System.out.println("1. See available books");
                System.out.println("2. Add a book");
                System.out.println("Enter your choice (Type 'exit' to finish): ");
                choice = scanner.nextLine();
                switch (choice) {
                    case "1":
                        // Send request to server to see available books
                        Writer.write("SEE_BOOKS");
                        Writer.newLine();
                        Writer.flush();
                        break;
                    case "2":
                        // Get book details from user and send request to add the book
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
                                price = Double.parseDouble(scanner.nextLine());
                                validPrice = true;
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
                        Writer.write("ADD_BOOK," + title + "," + author + "," + genre + "," + price + "," + quantity);
                        Writer.newLine();
                        Writer.flush();
                        break;
                    case "exit":
                        // Exit loop
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } while (!choice.equals("exit"));

            // Wait for the receiveThread to finish
            receiveThread.join();

        } catch (IOException | InterruptedException e) {
            System.out.println("Error communicating with server: " + e.getMessage());
        }
    }

    public static void main(String []args){
        BookClient bookClient = new BookClient("localhost", 6666);
        bookClient.startClient();
    }
}
