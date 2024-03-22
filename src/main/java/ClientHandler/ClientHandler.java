package ClientHandler;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private Socket socket;
    private BufferedWriter writer;
    public ClientHandler(Socket socket){
        this.socket=socket;
    }
    public BufferedWriter getWriter(){return writer;}
    @Override
    public void run() {
        try {
            BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //close
            reader.close();
            writer.close();
            socket.close();
            //put functions which will handle multible req 3and al server
        } catch (IOException i) {
            System.out.println("Error Handling Client: "+i.getMessage());
        }

    }
}
