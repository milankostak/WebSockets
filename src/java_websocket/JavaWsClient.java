package java_websocket;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import model.Message;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://github.com/TooTallNate/Java-WebSocket
public class JavaWsClient extends WebSocketClient {

    private JavaWsClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        send("Hello world");
        System.out.println("new connection opened");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
    }


    private int i = 0;
    private long sum  = 0;
    private String pattern = "\\{\"timestamp\":(\\d+)}";
    private Pattern r = Pattern.compile(pattern);
    private static Gson GSON = new Gson();
    private final int count = 1000000;

    @Override
    public void onMessage(String message) {
        //System.out.println("received message: " + message);
        //Message m = messageDecoder.decode(message);

        /*Matcher matcher = r.matcher(message);
        Message m = new Message();
        if (matcher.find()) {
            m.setTimestamp(Long.parseLong(matcher.group(1)));
        }*/

        Message m = GSON.fromJson(message, Message.class);

        sum += new Date().getTime() - m.getTimestamp();

        if (++i >= count) {
            System.out.print("Mean: ");
            System.out.println(sum / (float) count);
            System.out.print("Last: ");
            System.out.println(new Date().getTime() - m.getTimestamp());
        }
    }

    @Override
    public void onMessage(ByteBuffer message) {
        System.out.println("received ByteBuffer");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }

    public static void main(String[] args) throws URISyntaxException {
        WebSocketClient client = new JavaWsClient(new URI("ws://localhost:8887"));
        client.connect();
        //client.close();
        //client.closeBlocking();
    }
}