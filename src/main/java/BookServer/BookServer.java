package BookServer;

import BookClient.BookClient;
import ClientHandler.ClientHandler;
import DBConnection.DB;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BookServer {
    private static List<ClientHandler> users = new ArrayList<>();

    public static void main(String[] args) {
        try {
            DB.initializeDatabaseConnection();
            int port = 6666;
            System.out.println("MULTI THREAD SERVER IS UP.....");

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket,new BookClient("localhost",6666));
                    users.add(clientHandler);
                    Thread t = new Thread(clientHandler);
                    t.start();
                    System.out.println("Thread #" + t.getId());
                }
            }
        } catch (IOException i) {
            System.out.println("Server error: " + i.getMessage());
        }
    }

//    public static void notifyBookAdded(String message) {
//        // Notify all connected clients that a book has been added
//        for (ClientHandler client : users) {
//            try {
//                client.getWriter().write(message);
//                client.getWriter().newLine();
//                client.getWriter().flush();
//            } catch (IOException e) {
//                System.out.println("Error notifying client: " + e.getMessage());
//            }
//        }
//    }

    public static void notifyLoginResult(String username, int statusCode, ClientHandler clientHandler) {
        // Notify the client about login result with appropriate status code
        try {
            clientHandler.getWriter().write("LOGIN_RESULT," + statusCode + "," + username);
            clientHandler.getWriter().newLine();
            clientHandler.getWriter().flush();
        } catch (IOException e) {
            System.out.println("Error notifying client: " + e.getMessage());
        }
    }
    public static void notifyRegisterResult(String username, boolean success, String failureReason, ClientHandler clientHandler) {
        // Notify the client about registration result with appropriate message
        try {
            if (success) {
                clientHandler.getWriter().write("REGISTER_SUCCESS," + username);
            } else {
                clientHandler.getWriter().write("REGISTER_FAILURE," + username + "," + failureReason);
            }
            clientHandler.getWriter().newLine();
            clientHandler.getWriter().flush();
        } catch (IOException e) {
            System.out.println("Error notifying client: " + e.getMessage());
        }
    }
}