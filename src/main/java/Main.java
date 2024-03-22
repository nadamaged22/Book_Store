//import BookClient.BookClient;
//
//public class Main {
//    public static void main(String[] args) {
//        // Specify the host and port where the server is running
//        String host = "localhost"; // Assuming server is running on the same machine
//        int port = 6666; // Port where the server is listening
//
//        // Create a BookClient instance
//        BookClient bookClient = new BookClient(host, port);
//
//        // Example book details
//        String title = "Sample Book";
//        String author = "Sample Author";
//        String genre = "Sample Genre";
//        double price = 19.99;
//        int quantity = 100;
//
//        // Create a request string to add a book
//        String request = "ADD_BOOK," + title + "," + author + "," + genre + "," + price + "," + quantity;
//
//        // Send the request to the server
//        bookClient.sendRequest(request);
//    }
//}
