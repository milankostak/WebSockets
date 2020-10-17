package tyrus;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://github.com/tyrus-project/tyrus
@ClientEndpoint
public class TyrusClient {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private MessageDecoder messageDecoder = new MessageDecoder();
    private static CountDownLatch latch;

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Connected ... " + session.getId());
        try {
            session.getBasicRemote().sendText("start");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int i = 0;
    private long sum  = 0;
    private final String pattern = "\\{\"timestamp\":(\\d+)}";
    private final Pattern r = Pattern.compile(pattern);

    @OnMessage
    public String onMessage(String message, Session session) {
        //LOGGER.info("Received ...." + message);
        //Message m = messageDecoder.decode(message);
        Matcher m = r.matcher(message);
        if (m.find()) {
            sum += new Date().getTime() - Long.parseLong(m.group(1));
        }
        //sum += new Date().getTime() - m.getTimestamp();
        if (++i >= 1000) {
            System.out.print("Mean: ");
            System.out.println(sum / 1000.0);
            System.out.print("Last: ");
            System.out.println(new Date().getTime() - Long.parseLong(m.group(1)));
            //System.out.println(new Date().getTime() - m.getTimestamp());
            latch.countDown();
        }
        return "";
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOGGER.info(String.format("Session %s close because of %s", session.getId(), closeReason));
    }

    public static void main(String[] args) {
        latch = new CountDownLatch(1);

        ClientManager client = ClientManager.createClient();
        try {
            client.connectToServer(TyrusClient.class, new URI("ws://127.0.0.1:8025/chat/franta"));
            latch.await();

        } catch (DeploymentException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}