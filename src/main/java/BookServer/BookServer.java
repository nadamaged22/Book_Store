package BookServer;

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
                    ClientHandler clientHandler = new ClientHandler(socket);
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
}
