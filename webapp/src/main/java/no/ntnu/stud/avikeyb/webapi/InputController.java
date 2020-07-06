package no.ntnu.stud.avikeyb.webapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


public class InputController extends TextWebSocketHandler {

    @Autowired
    KeyboardAPI keyboardAPI;


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        keyboardAPI.sendInput(message.getPayload());
    }


}
