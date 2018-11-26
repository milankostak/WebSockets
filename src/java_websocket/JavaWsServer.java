package java_websocket;

import com.google.gson.Gson;
import model.Message;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Date;

// https://github.com/TooTallNate/Java-WebSocket
public class JavaWsServer extends WebSocketServer {

    private static Gson GSON = new Gson();

    private JavaWsServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        //conn.send("Welcome to the server!"); //This method sends a message to the new client
        //broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
        System.out.println("new connection to " + conn.getRemoteSocketAddress());

        new Thread(() -> {
            int i = 0;
            long time = new Date().getTime();
            while (i++ < 1000000) {
                Message m = new Message();
                m.setTimestamp(new Date().getTime());
                conn.send(GSON.toJson(m));
                /*try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
            System.out.println(new Date().getTime() - time);
        }).start();

        /*Message message = new Message();
        message.setFrom(username);
        message.setContent("Connected!");
        broadcast(message);*/
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("received message from " + conn.getRemoteSocketAddress() + ": " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        System.out.println("received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occured on connection " + conn.getRemoteSocketAddress() + ":" + ex);
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8887;

        JavaWsServer server = new JavaWsServer(new InetSocketAddress(host, port));
        server.start();
        //server.stop();
    }
}