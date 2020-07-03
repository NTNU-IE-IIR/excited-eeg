package no.ntnu.stud.avikeyb.webapi;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Layout;
import no.ntnu.stud.avikeyb.backend.OutputDevice;
import no.ntnu.stud.avikeyb.backend.core.CoreKeyboard;
import no.ntnu.stud.avikeyb.backend.dictionary.DictionaryHandler;
import no.ntnu.stud.avikeyb.backend.dictionary.ResourceHandler;
import no.ntnu.stud.avikeyb.backend.layouts.BinarySearchLayout;
import no.ntnu.stud.avikeyb.backend.layouts.util.LayoutState;
import no.ntnu.stud.avikeyb.backend.outputs.ConsoleOutput;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper around the backend to make it safe thread safe. All calls to the backend must go through and instance
 * of this class
 */
@Component
public class KeyboardAPI {

    private Keyboard keyboard;
    private BinarySearchLayout layout;

    private Set<WebSocketSession> outputs = new HashSet<>();
    private Set<WebSocketSession> uiSessions = new HashSet<>();

    @PostConstruct
    public void init() {

        keyboard = new CoreKeyboard();
        keyboard.addOutputDevice(new ConsoleOutput());
        keyboard.addOutputDevice(new OutputDevice() {
            @Override
            public void sendOutput(String output) {
                notifyOutputs(output);
            }
        });

        DictionaryHandler dictionary = new DictionaryHandler();

        try {

            dictionary.setDictionary(ResourceHandler.loadDictionaryFromStream(dictionary.getClass().getClassLoader().getResourceAsStream("dictionary.txt"))); // This should load the dictionary.txt from the backend project
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        layout = new BinarySearchLayout(keyboard);

        keyboard.addStateListener(new Keyboard.KeyboardListener() {
            @Override
            public void onOutputBufferChange(String oldBuffer, String newBuffer) {
                layout.setSuggestions(dictionary.getSuggestionsStartingWith(keyboard.getCurrentWord()));
            }
        });

        layout.addLayoutListener(new Layout.LayoutListener() {
            @Override
            public void onLayoutChanged() {
                notifyUI(getLayoutState());
            }
        });
    }


    // ...
    @PreDestroy
    public void destroy() {
        // Save dictionary etc...
    }


    public synchronized void addOutputSession(WebSocketSession session) {
        outputs.add(session);
    }

    public synchronized void removeOutputSession(WebSocketSession session) {
        outputs.remove(session);
    }

    public synchronized void addUISession(WebSocketSession session) {
        uiSessions.add(session);
    }

    public synchronized void removeUISession(WebSocketSession session) {
        uiSessions.remove(session);
    }


    public synchronized void sendInput(String inputMessage) {
        switch (inputMessage) {
            case "1":
                sendInput(InputType.INPUT1);
                break;
            case "2":
                sendInput(InputType.INPUT2);
                break;
            case "3":
                sendInput(InputType.INPUT3);
                break;
            case "4":
                sendInput(InputType.INPUT4);
                break;
        }
    }

    public synchronized void sendInitialLayoutState(WebSocketSession session){
        sendMessage(session, getLayoutState()); // initial state on connect
    }

    private String getLayoutState(){
        LayoutState state = layout.getCurrentLayoutInternalState();
        state.add("output_buffer", keyboard.getCurrentBuffer());
        return LayoutStateConverter.stateToJSON(state);
    }

    private void sendInput(InputType input) {
        layout.sendInputSignal(input);
    }


    private void notifyOutputs(String output) {
        if (output.isEmpty()) {
            return;
        }
        for (WebSocketSession ses : outputs) {
            sendMessage(ses, output);
        }
    }

    private void notifyUI(String output) {
        if (output.isEmpty()) {
            return;
        }
        for (WebSocketSession ses : uiSessions) {
            sendMessage(ses, output);
        }
    }

    private void sendMessage(WebSocketSession session, String message){
        try {
            session.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
