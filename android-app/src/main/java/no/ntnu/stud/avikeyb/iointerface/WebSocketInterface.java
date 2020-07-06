package no.ntnu.stud.avikeyb.iointerface;

import android.os.Handler;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import no.ntnu.stud.avikeyb.backend.InputInterface;
import no.ntnu.stud.avikeyb.backend.InputType;

/**
 * Generic web socket input interface for the android app
 * <p>
 * We only want a single instance of the server running, a singleton is used to achieve this
 */
public class WebSocketInterface {

    public final static int INTERFACE_TCP_PORT = 43879;

    private static WebSocketInterface instance;
    private static boolean serverStarted = false;


    public static WebSocketInterface getInstance() {
        if (instance == null) {
            instance = new WebSocketInterface();
        }
        return instance;
    }


    private SocketServer socketServer;
    private InputInterface inputInterface;
    private Handler inputHandler;


    /**
     * Sets the input insterface to forward the incoming input to
     *
     * @param handler the handler for the thread that should receive the inputs
     * @param input   the input interface to forward the incoming inputs to
     */
    public synchronized void setInputInterface(Handler handler, InputInterface input) {
        this.inputHandler = handler;
        this.inputInterface = input;
    }


    /**
     * Sends output to the connected output sockets
     *
     * @param output the output to send
     */
    public void sendOutput(String output) {

        // This may have to be done in its own thread
        for (WebSocket con : socketServer.outputSockets) {
            con.send(output);
        }
    }


    /**
     * Starts the local socket server
     */
    public void start() {
        if (serverStarted) {
            return;
        }
        startNewServer();
    }


    /**
     * Stops the local socket server
     */
    public void stop() {
        if (!serverStarted) {
            return;
        }
        try {
            closeAllConnections();
            socketServer.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("WEBSOCKET", "Closing server on port " + socketServer.getPort());
        serverStarted = false;
        socketServer = null;
    }


    // The server can only be started once so we have to create a new instance on every restart
    private void startNewServer() {
        serverStarted = true;
        socketServer = new SocketServer(INTERFACE_TCP_PORT);
        socketServer.start();
        Log.d("WEBSOCKET", "Listening on port " + socketServer.getPort());

    }

    private void closeAllConnections() {
        // The connections collection must be copied before iterating because the collection
        // changes as the connections are closed
        for (WebSocket con : new ArrayList<>(socketServer.connections())) {
            con.closeConnection(CloseFrame.NORMAL, "Server is stopping");
        }
    }


    private class SocketServer extends WebSocketServer {

        Collection<WebSocket> outputSockets = Collections.newSetFromMap(new ConcurrentHashMap<WebSocket, Boolean>());
        Collection<WebSocket> inputSockets = Collections.newSetFromMap(new ConcurrentHashMap<WebSocket, Boolean>());


        public SocketServer(int port) {
            super(new InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Log.d("WEBSOCKET", "Client Connected " + handshake.getResourceDescriptor());
            if (handshake.getResourceDescriptor().equals("/output")) {
                outputSockets.add(conn);
            } else if (handshake.getResourceDescriptor().equals("/input")) {
                inputSockets.add(conn);
            }
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Log.d("WEBSOCKET", "Client Disconnected. Reason: " + reason);
            outputSockets.remove(conn);
            inputSockets.remove(conn);

        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            Log.d("WEBSOCKET", "Receiving: " + message);

            synchronized (WebSocketInterface.this) {

                if (inputInterface == null) {
                    return;
                }

                final InputType input = convertToInputType(message);

                inputHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (WebSocketInterface.this) {
                            inputInterface.sendInputSignal(input);
                        }
                    }
                });
            }
        }

        private InputType convertToInputType(String message) {
            switch (message.trim()) {
                case "1":
                    return InputType.INPUT1;
                case "2":
                    return InputType.INPUT2;
                case "3":
                    return InputType.INPUT3;
                case "4":
                    return InputType.INPUT4;
                default:
                    return null;
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            Log.d("WEBSOCKET", "Error: " + ex.getMessage());
        }
    }


}


