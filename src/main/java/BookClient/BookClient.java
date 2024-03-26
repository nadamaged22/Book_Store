package BookClient;

import DB_Methods.DBMethods;
import org.bson.Document;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class BookClient {
    private int port;
    private String host;
    private boolean loggedIn;
    private String currentUser;

    public BookClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.loggedIn = false;
    }

    public void startClient() {
        try (Socket socket = new Socket(host, port)) {
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Scanner scanner = new Scanner(System.in);

            while (true) {
                if (!loggedIn) {
                    displayMenu();
                    String choice = scanner.nextLine();
                    switch (choice) {
                        case "1":
                            registerUser(writer, serverReader, scanner);
                            break;
                        case "2":
                            loginUser(writer, serverReader, scanner);
                            break;
                        case "exit":
                            return; // Exit the method and close the socket
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                } else {
                        displayBookMenu();
                    String choice = scanner.nextLine();
                    switch (choice) {
                        case "1":
                            addBook(writer, serverReader, scanner);
                            break;
                        case "2":
                            removeBook(writer, serverReader, scanner);
                            break;
                        case "3":
                            seeAvailableBooks(writer,serverReader);
                        case "4":
                            searchBooks(scanner,writer,serverReader);
                            break;
                        case "logout":
                            loggedIn = false;
                            break;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error communicating with server: " + e.getMessage());
        }
    }

    private void displayMenu() {
        System.out.println("Menu:");
        System.out.println("1. SignUP");
        System.out.println("2. Login");
        System.out.println("Type 'exit' to finish.");
        System.out.print("Enter your choice: ");

    }
    private void displayBookMenu() {
        System.out.println("\nBook Menu:");
        System.out.println("1. Add Book");
        System.out.println("2. Remove Book");
        System.out.println("3. Show All Books");
        System.out.println("4. Search For Book");
        System.out.println("Type 'logout' to logout.");
        System.out.print("Enter your choice: ");
    }
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }


    private void registerUser(BufferedWriter writer, BufferedReader serverReader, Scanner scanner) throws IOException {
        System.out.println("Enter user details:");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        // Ask for username and validate uniqueness
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.println("Select Role:");
        System.out.println("1. Admin");
        System.out.println("2. User");
        int roleChoice = 0;
        boolean validChoice = false;

        while (!validChoice) {
            try {
                System.out.print("Enter choice (1 for Admin, 2 for User): ");
                roleChoice = Integer.parseInt(scanner.nextLine());
                if (roleChoice == 1 || roleChoice == 2) {
                    validChoice = true;
                } else {
                    System.out.println("Invalid choice. Please enter 1 for Admin or 2 for User.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        // Map role choice to role string
        String role = (roleChoice == 1) ? "admin" : "user";

        // Send request to server to register the user
        writer.write("REGISTER_USER," + name + "," + username + "," + password + "," + role);
        writer.newLine();
        writer.flush();

        // Read response from server
        String response = serverReader.readLine();
        if (response != null) {
            String[] responseParts = response.split(",");
            if (responseParts.length >= 2) {
                String registerStatus = responseParts[0];
                String user = responseParts[1];
                if (registerStatus.equals("REGISTER_SUCCESS")) {
                    setCurrentUser(user);
                    System.out.println("Registration successful for user: " + user);
                    loggedIn = true;

                } else if (registerStatus.equals("REGISTER_FAILURE")) {
                    String failureReason = responseParts.length >= 3 ? responseParts[2] : "Reason not specified";
                    System.out.println("Registration failed for user: " + user + ". Reason: " + failureReason);
                } else {
                    System.out.println("Invalid response from server.");
                }
            } else {
                System.out.println("Invalid response from server.");
            }
        } else {
            System.out.println("No response from server.");
        }
    }
    private void loginUser(BufferedWriter writer, BufferedReader serverReader, Scanner scanner) throws IOException {
        System.out.println("Enter login details:");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Send request to server to check credentials
        writer.write("LOGIN_USER," + username + "," + password);
        writer.newLine();
        writer.flush();

        // Read response from server
        String response = serverReader.readLine();
        if (response != null) {
            String[] responseParts = response.split(",");
            if (responseParts.length >= 3) {
                String loginStatus = responseParts[0];
                int statusCode = Integer.parseInt(responseParts[1]);
                String user = responseParts[2];
                if (loginStatus.equals("LOGIN_RESULT")) {
                    if (statusCode == 200) {
                        setCurrentUser(user);
                        System.out.println("Login successful for user: " + user +" "+ "statusCode: "+statusCode);
                        loggedIn = true;

                    } else {
                        System.out.println("Login failed for user: " + user +" "+ "statusCode: "+ statusCode);
                    }
                } else {
                    System.out.println("Invalid response from server.");
                }
            } else {
                System.out.println("No response from server.");
            }
        }
    }
    public String getCurrentUser() {
        return currentUser;
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
    private void searchBooks(Scanner scanner, BufferedWriter writer, BufferedReader serverReader) throws IOException {
        String keyword;
        String field;

        boolean searchAgain = true;

        while (searchAgain) {
            System.out.println("Search by:\n1. Title\n2. Author\n3. Genre");
            int choice;
            do {
                System.out.print("Enter your choice: ");
                choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        field = "title";
                        break;
                    case 2:
                        field = "author";
                        break;
                    case 3:
                        field = "genre";
                        break;
                    default:
                        System.out.println("Invalid choice. Please choose a number between 1 and 3.");
                        continue; // Prompt the user again
                }

                System.out.println("Enter search keyword:");
                keyword = scanner.nextLine().trim();

                // Send request to server to search for books
                writer.write("SEARCH_BOOKS," + keyword + "," + field);
                writer.newLine();
                writer.flush();

                String response;
                boolean foundResults = false; // Track if any results were found
                System.out.println("Search results:");
                while ((response = serverReader.readLine()) != null) {
                    if (response.equals("END_OF_SEARCH")) {
                        if (!foundResults) {
                            System.out.println("No matching books found.");
                        }
                        break; // Stop reading when encountering the delimiter
                    }
                    foundResults = true;
                    System.out.println(response);
                }
                if (response == null) {
                    System.out.println("No matching books found.");
                }
            } while (choice < 1 || choice > 3);

            System.out.println("Do you want to search again? (yes/no)");
            String searchAgainInput = scanner.nextLine().trim().toLowerCase();
            searchAgain = searchAgainInput.equals("yes");
        }
    }

    private void seeAvailableUsers(BufferedWriter writer, BufferedReader serverReader) throws IOException {
        writer.write("SEE_USERS");
        writer.newLine();
        writer.flush();
        String response;
        System.out.println("Available users:");
        while ((response = serverReader.readLine()) != null) {
            if (response.equals("END_OF_USERS")) {
                break; // Stop reading when encountering the delimiter
            }
            System.out.println(response);
        }
    }

    private void addBook(BufferedWriter writer, BufferedReader serverReader ,Scanner scanner) throws IOException {

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
        String currentUser = getCurrentUser();
        // Send request to server to add the book
        writer.write("ADD_BOOK," + title + "," + author + "," + genre + "," + price + "," + quantity + "," + currentUser);
        writer.newLine();
        writer.flush();
        // Read response from server
        String response = serverReader.readLine();
        if (response != null) {
            String[] responseParts = response.split(",");
            if (responseParts.length >= 2) {
                String addBookStatus = responseParts[0];
                String bookTitle = responseParts[1];
                if (addBookStatus.equals("BOOK_ADDED")) {
                    System.out.println("Book added successfully: " + bookTitle);
                } else {
                    System.out.println("Failed to add book: " + bookTitle);
                }
            } else {
                System.out.println("Invalid response from server.");
            }
        } else {
            System.out.println("No response from server.");
        }
    }

    private void removeBook(BufferedWriter writer, BufferedReader serverReader, Scanner scanner) throws IOException {
        System.out.println("Enter the title of the book you want to remove:");
        String title = scanner.nextLine();

        String currentUser = getCurrentUser(); // Get the current user's username

        // Send request to server to remove the book
        writer.write("REMOVE_BOOK," + title + "," + currentUser); // Include currentUser parameter
        writer.newLine();
        writer.flush();

        // Read response from server
        String response = serverReader.readLine();
        if (response != null) {
            String[] responseParts = response.split(",");
            if (responseParts.length >= 2) {
                String removalStatus = responseParts[0];
                String removedBookTitle = responseParts[1];
                if (removalStatus.equals("BOOK_REMOVED")) {
                    System.out.println("Book removed: " + removedBookTitle);
                } else if (removalStatus.equals("BOOK_CAN_NOT_BE_REMOVED")) {
                    System.out.println("Book can not be removed: " + removedBookTitle);
                } else if (removalStatus.equals("REMOVAL_FAILED")) { // New status for failed removal due to permissions
                    System.out.println("Failed to remove book: " + removedBookTitle + ". You do not have permission to remove this book.");
                } else {
                    System.out.println("Invalid response from server.");
                }
            } else {
                System.out.println("Invalid response from server.");
            }
        } else {
            System.out.println("No response from server.");
        }
    }


    public static void main(String[] args) {
        BookClient bookClient = new BookClient("localhost", 6666);
        bookClient.startClient();
    }
}
