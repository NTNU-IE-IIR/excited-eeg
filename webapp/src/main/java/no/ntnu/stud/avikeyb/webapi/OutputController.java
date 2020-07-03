package no.ntnu.stud.avikeyb.webapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


public class OutputController extends TextWebSocketHandler {

    @Autowired
    KeyboardAPI keyboardAPI;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        keyboardAPI.addOutputSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        keyboardAPI.removeOutputSession(session);

    }
}
