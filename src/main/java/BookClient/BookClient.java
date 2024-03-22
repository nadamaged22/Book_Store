package BookClient;

import java.net.InetAddress;

public class BookClient {
    private int port;
    private static InetAddress host;
    public BookClient(InetAddress host,int port){
        this.host=host;
        this.port=port;
    }
}
