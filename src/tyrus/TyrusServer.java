package tyrus;

import model.Message;
import org.glassfish.tyrus.server.Server;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

// https://github.com/tyrus-project/tyrus
@ServerEndpoint(
        value = "/chat/{username}",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class)
public class TyrusServer {

    private Session session;
    private final Set<TyrusServer> chatEndpoints = new CopyOnWriteArraySet<>();
    private final HashMap<String, String> users = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        System.out.println(username);
        this.session = session;
        chatEndpoints.add(this);
        users.put(session.getId(), username);

        new Thread(() -> {
            int i = 0;
            long time = new Date().getTime();
            while (i++ < 1000) {
                Message m = new Message();
                m.setTimestamp(new Date().getTime());
                broadcast(m);
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(new Date().getTime() - time);
        }).start();

        /*Message message = new Message();
        message.setFrom(username);
        message.setContent("Connected!");
        broadcast(message);*/
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        System.out.println("onMessage");

        message.setFrom(users.get(session.getId()));
        broadcast(message);
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("onClose");

        chatEndpoints.remove(this);
        Message message = new Message();
        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");
        broadcast(message);

        users.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.out.println("onError");
        t.printStackTrace();
    }

    private void broadcast(Message message) {

        chatEndpoints.forEach(endpoint -> {
            try {
                endpoint.session.getBasicRemote().sendObject(message);
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        Server server = new Server("127.0.0.1", 8025, "/", TyrusServer.class);

        try {
            server.start();
            System.out.print("Please press a key to stop the server.");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            server.stop();
        }
    }
}