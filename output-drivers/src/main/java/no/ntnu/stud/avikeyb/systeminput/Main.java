package no.ntnu.stud.avikeyb.systeminput;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by pitmairen on 28/03/2017.
 */
public class Main {


    public static void main(String[] args) throws URISyntaxException, InterruptedException, AWTException {
        //WebSocketImpl.DEBUG = true;
        if (args.length == 0) {
            System.out.println("Usage java -jar systeminput.jar ws://example.com/output");
            System.exit(1);
        }

        Main main = new Main(new URI(args[0]));
        main.run();
    }


    private final URI keyboardOutputSocketURI;
    private final static HashMap<Character, Integer> characterToKeyEventMap = new HashMap<>();
    private final BlockingQueue<String> outputBuffer;


    public Main(URI keyboardOutputSocketURI) throws AWTException {
        this.keyboardOutputSocketURI = keyboardOutputSocketURI;
        outputBuffer = new LinkedBlockingQueue<>();
    }


    private void run() throws InterruptedException {

        Thread socketThread = new Thread(new SocketConnection());
        Thread writerThread = new Thread(new RobotWriter());

        socketThread.start();
        writerThread.start();

        socketThread.join();
        writerThread.join();

    }


    private class RobotWriter implements Runnable {

        private Robot robot;

        @Override
        public void run() {

            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
                return;
            }

            while (true) {
                try {
                    consumeNext();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }


        }

        private void consumeNext() throws InterruptedException {

            String next = outputBuffer.take();

            for (int i = 0; i < next.length(); i++) {
                Character c = next.charAt(i);
                if (characterToKeyEventMap.containsKey(c)) {
                    robot.keyPress(characterToKeyEventMap.get(c));
                    robot.keyRelease(characterToKeyEventMap.get(c));
                }
            }
        }

    }


    private class SocketConnection implements Runnable {

        private WebSocketClient ws;
        private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();


        @Override
        public void run() {

            try {
                connectToServer();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            while (true) {
                try {
                    String next = messages.poll(2, TimeUnit.SECONDS);
                    if (next != null) {
                        outputBuffer.put(next);
                    }

                    if (ws.getReadyState() == WebSocket.READYSTATE.CLOSED) {
                        System.out.println("Reconnecting...");
                        connectToServer();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }


        private void connectToServer() throws InterruptedException {

            System.out.println("Trying to connect to server " + keyboardOutputSocketURI);

            ws = new WebSocketClient(keyboardOutputSocketURI, new Draft_17()) {
                @Override
                public void onMessage(String message) {
                    System.out.println("received " + message);
                    try {
                        messages.put(message);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to server");
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Connection to server closed.");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            ws.connect();

        }

    }


    // Helper method for adding character to event mappings
    private static void m(Character character, int event) {
        characterToKeyEventMap.put(character, event);
    }

    static {

        m('a', KeyEvent.VK_A);
        m('b', KeyEvent.VK_B);
        m('c', KeyEvent.VK_C);
        m('d', KeyEvent.VK_D);
        m('e', KeyEvent.VK_E);
        m('f', KeyEvent.VK_F);
        m('g', KeyEvent.VK_G);
        m('h', KeyEvent.VK_H);
        m('i', KeyEvent.VK_I);
        m('j', KeyEvent.VK_J);
        m('k', KeyEvent.VK_K);
        m('l', KeyEvent.VK_L);
        m('m', KeyEvent.VK_M);
        m('n', KeyEvent.VK_N);
        m('o', KeyEvent.VK_O);
        m('p', KeyEvent.VK_P);
        m('q', KeyEvent.VK_Q);
        m('r', KeyEvent.VK_R);
        m('s', KeyEvent.VK_S);
        m('t', KeyEvent.VK_T);
        m('u', KeyEvent.VK_U);
        m('v', KeyEvent.VK_V);
        m('w', KeyEvent.VK_W);
        m('x', KeyEvent.VK_X);
        m('y', KeyEvent.VK_Y);
        m('z', KeyEvent.VK_Z);
        m('1', KeyEvent.VK_1);
        m('2', KeyEvent.VK_2);
        m('3', KeyEvent.VK_3);
        m('4', KeyEvent.VK_4);
        m('5', KeyEvent.VK_5);
        m('6', KeyEvent.VK_6);
        m('7', KeyEvent.VK_7);
        m('8', KeyEvent.VK_8);
        m('9', KeyEvent.VK_9);
        m('0', KeyEvent.VK_0);
        m(' ', KeyEvent.VK_SPACE);
        m('!', KeyEvent.VK_EXCLAMATION_MARK);
        m(',', KeyEvent.VK_COMMA);
        m('.', KeyEvent.VK_PERIOD);

    }
}
