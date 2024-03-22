package BookServer;

import ClientHandler.ClientHandler;

import javax.imageio.IIOException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BookServer {
    private static List<ClientHandler>Users=new ArrayList<>();
    public static void main(String[] args) {
        try {
            int port=6666;
            System.out.println("MULTI THREAD SERVER IS UP.....");
            try (ServerSocket serverSocket=new ServerSocket(port)){
                while (true){
                    Socket socket=serverSocket.accept();
                    ClientHandler clientHandler=new ClientHandler(socket);
                    Users.add(clientHandler);
                    Thread t=new Thread(clientHandler);
                    t.start();
                    System.out.println("Thread #"+t.getId());
                }

            }
        }catch (IOException i){
            System.out.println("Server error: "+i.getMessage());
        }




    }
    //implemnt functions
}
