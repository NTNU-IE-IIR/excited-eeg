package no.ntnu.stud.avikeyb.gui.core;

import org.junit.Before;
import org.junit.Test;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.OutputDevice;
import no.ntnu.stud.avikeyb.backend.core.CoreKeyboard;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by pitmairen on 04/04/2017.
 */
public class ChessLayoutTest {

    private Keyboard keyboard;
    private ChessLayout chessLayout;


    @Before
    public void setUp() throws Exception {
        keyboard = new CoreKeyboard();
        chessLayout = new ChessLayout(keyboard);
    }

    @Test
    public void testSelectA() throws Exception {
        moveLeft(4);
        selectLeft();
        assertEquals("a", keyboard.getCurrentBuffer());
    }

    @Test
    public void testSelectB() throws Exception {
        moveLeft(3);
        selectLeft();
        assertEquals("b", keyboard.getCurrentBuffer());
    }

    @Test
    public void testSelectC() throws Exception {
        moveLeft(2);
        selectLeft();
        assertEquals("c", keyboard.getCurrentBuffer());
    }

    @Test
    public void testSelectD() throws Exception {
        moveLeft(1);
        selectLeft();
        assertEquals("d", keyboard.getCurrentBuffer());
    }

    @Test
    public void testSelectE() throws Exception {
        moveRight(1);
        selectRight();
        assertEquals("e", keyboard.getCurrentBuffer());
    }

    @Test
    public void testSelectF() throws Exception {
        moveRight(2);
        selectRight();
        assertEquals("f", keyboard.getCurrentBuffer());
    }

    @Test
    public void testSelectG() throws Exception {
        moveRight(3);
        selectRight();
        assertEquals("g", keyboard.getCurrentBuffer());
    }

    @Test
    public void testSelectH() throws Exception {
        moveRight(4);
        selectRight();
        assertEquals("h", keyboard.getCurrentBuffer());
    }

    @Test
    public void testNumbers() throws Exception {
        for (int i = 0; i < 4; i++) {
            moveLeft(4);
            selectLeft();
            moveLeft(i + 1);
            selectLeft();
            assertEquals("a" + (4 - i), keyboard.getCurrentBuffer());

            // Clear buffer
            moveRight(1);
            selectRight();
        }
        for (int i = 0; i < 4; i++) {
            moveLeft(4);
            selectLeft();
            moveRight(i + 1);
            selectRight();
            assertEquals("a" + (5 + i), keyboard.getCurrentBuffer());

            // Clear buffer
            moveRight(1);
            selectRight();
        }
    }

    @Test
    public void testClear() throws Exception {
        moveLeft(1);
        selectLeft();
        moveRight(1);
        selectRight();
        assertEquals("d5", keyboard.getCurrentBuffer());
        moveRight(1);
        selectRight();
        assertEquals("", keyboard.getCurrentBuffer());
    }

    @Test
    public void testSend() throws Exception {
        moveLeft(1);
        selectLeft();
        moveRight(1);
        selectRight();
        assertEquals("d5", keyboard.getCurrentBuffer());

        final boolean[] wasCalled = {false};
        keyboard.addOutputDevice(new OutputDevice() {
            @Override
            public void sendOutput(String s) {
                assertEquals("d5", keyboard.getCurrentBuffer());
                wasCalled[0] = true;
            }
        });

        moveLeft(1);
        selectLeft();

        assertTrue(wasCalled[0]);
    }


    @Test
    public void testRollover() throws Exception {

        moveLeft(6);
        selectLeft();
        moveRight(7);
        selectRight();
        assertEquals("g3", keyboard.getCurrentBuffer());


        final boolean[] wasCalled = {false};
        keyboard.addOutputDevice(new OutputDevice() {
            @Override
            public void sendOutput(String s) {
                assertEquals("g3", keyboard.getCurrentBuffer());
                wasCalled[0] = true;
            }
        });

        moveLeft(4);
        selectLeft();

        assertTrue(wasCalled[0]);
    }


    private void moveLeft(int steps) {
        for (int i = 0; i < steps; i++) {
            chessLayout.sendInputSignal(InputType.INPUT1);
        }
    }

    private void moveRight(int steps) {
        for (int i = 0; i < steps; i++) {
            chessLayout.sendInputSignal(InputType.INPUT2);
        }
    }

    private void selectLeft() {
        chessLayout.sendInputSignal(InputType.INPUT2);
    }

    private void selectRight() {
        chessLayout.sendInputSignal(InputType.INPUT1);
    }
}