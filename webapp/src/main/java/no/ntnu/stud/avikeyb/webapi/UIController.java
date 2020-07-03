package no.ntnu.stud.avikeyb.webapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


public class UIController extends TextWebSocketHandler {

    @Autowired
    KeyboardAPI keyboard;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        keyboard.addUISession(session);
        keyboard.sendInitialLayoutState(session);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        keyboard.removeUISession(session);
    }


}
