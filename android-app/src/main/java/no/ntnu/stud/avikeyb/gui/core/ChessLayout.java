package no.ntnu.stud.avikeyb.gui.core;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.Symbols;
import no.ntnu.stud.avikeyb.backend.layouts.BaseLayout;

import static no.ntnu.stud.avikeyb.backend.Symbol.A;
import static no.ntnu.stud.avikeyb.backend.Symbol.B;
import static no.ntnu.stud.avikeyb.backend.Symbol.C;
import static no.ntnu.stud.avikeyb.backend.Symbol.CLEAR_BUFFER;
import static no.ntnu.stud.avikeyb.backend.Symbol.D;
import static no.ntnu.stud.avikeyb.backend.Symbol.E;
import static no.ntnu.stud.avikeyb.backend.Symbol.F;
import static no.ntnu.stud.avikeyb.backend.Symbol.G;
import static no.ntnu.stud.avikeyb.backend.Symbol.H;
import static no.ntnu.stud.avikeyb.backend.Symbol.NUM_1;
import static no.ntnu.stud.avikeyb.backend.Symbol.NUM_2;
import static no.ntnu.stud.avikeyb.backend.Symbol.NUM_3;
import static no.ntnu.stud.avikeyb.backend.Symbol.NUM_4;
import static no.ntnu.stud.avikeyb.backend.Symbol.NUM_5;
import static no.ntnu.stud.avikeyb.backend.Symbol.NUM_6;
import static no.ntnu.stud.avikeyb.backend.Symbol.NUM_7;
import static no.ntnu.stud.avikeyb.backend.Symbol.NUM_8;
import static no.ntnu.stud.avikeyb.backend.Symbol.SEND;
import static no.ntnu.stud.avikeyb.backend.Symbol.SETTING;

/**
 * Simple chess layout for chess notation
 * <p>
 * The layout has three modes. First the user will select the letter from A-H that represents the
 * column on the chess board. Then the user will select the number from 1-8 that represents
 * the row of the chess board. Finally the user either confirms or clears the current selection.
 * <p>
 * In each mode the user starts in the middle of the possible symbols and then goes left
 * or right to select symbols in the respective direction. To select the current symbol an input
 * in the opposite direction is used. E.g. To select the letter "c" move twice to the left using
 * input1 and select the symbol with input2. To select the letter "f" move twice to the right
 * using input2 and then select the symbol with input1.
 */
public class ChessLayout extends BaseLayout {

    public enum State {
        LETTER,
        NUMBER,
        CONFIRM
    }

    private Symbol[] letters = {A, B, C, D, E, F, G, H};
    private Symbol[] numbers = {NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8};
    private Symbol[] confirm = {SEND, CLEAR_BUFFER, SETTING};
    private Symbol[] allSymbols = Symbols.merge(letters, numbers, confirm);


    private State currentState = State.LETTER;
    private Integer currentPosition = null;
    private Symbol[] currentSymbols = letters;
    private InputType currentDirection = null;

    private Keyboard keyboard;


    public ChessLayout(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    /**
     * Checks if the position is in the initial selection position between two symbols
     *
     * @return true if in initial selection state
     */
    public boolean isInitialSelection() {
        return currentPosition == null;
    }

    /**
     * Returns the current position in the state
     * <p>
     * Must only be called if isInitialSelection() returns true
     *
     * @return the current position of the selection
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Retruns the current state
     *
     * @return the current state
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Retuens all symbols in the layout
     *
     * @return an array of symbols
     */
    public Symbol[] getSymbols() {
        return allSymbols;
    }


    @Override
    protected void onStep(InputType inputType) {

        if (checkForSelection(inputType)) {
            selectCurrent();
        } else {
            if (inputType == InputType.INPUT1) {
                moveLeft();
                currentDirection = inputType;
            } else if (inputType == InputType.INPUT2) {
                moveRight();
                currentDirection = inputType;
            }
        }
        notifyLayoutListeners();
    }

    // It is a selection when the user switches the input type (direction) while in the current mode
    private boolean checkForSelection(InputType inputType) {
        return (inputType == InputType.INPUT1 || inputType == InputType.INPUT2) && currentDirection != null && currentDirection != inputType;
    }

    private void selectCurrent() {

        if (currentState == State.CONFIRM) {
            if (currentSymbols[currentPosition] == SEND) {
                keyboard.sendCurrentBuffer();
            } else if (currentSymbols[currentPosition] == CLEAR_BUFFER) {
                keyboard.clearCurrentBuffer();
            }else if(currentSymbols[currentPosition] == SETTING){
                keyboard.requestChangeSettings();
            }
            reset(State.LETTER);
            return;
        }

        // Add current selection and go to next mode.
        keyboard.addToCurrentBuffer(currentSymbols[currentPosition].getContent());
        if (currentState == State.LETTER) {
            reset(State.NUMBER);
        } else if (currentState == State.NUMBER) {
            reset(State.CONFIRM);
        }
    }


    private void moveLeft() {
        // On the first move we start from the middle between the two letters in the middle
        if (currentPosition == null) {
            currentPosition = getInitialPosition() - 1;
        } else {
            currentPosition = getNewPosition(-1);
        }
    }

    private void moveRight() {
        // On the first move we start from the middle between the two letters in the middle
        if (currentPosition == null) {
            currentPosition = getInitialPosition();
        } else {
            currentPosition = getNewPosition(1);
        }
    }

    private void reset(State state) {
        this.currentState = state;
        this.currentPosition = null;
        this.currentDirection = null;
        updateCurrentSymbols();
    }

    private void updateCurrentSymbols() {
        switch (currentState) {
            case LETTER:
                currentSymbols = letters;
                break;
            case NUMBER:
                currentSymbols = numbers;
                break;
            case CONFIRM:
                currentSymbols = confirm;
                break;
        }
    }


    private int getInitialPosition() {
        return currentSymbols.length / 2;
    }

    // add "addition" to the current position and wrap around if the result is
    // outside the current symbols
    private int getNewPosition(int addition) {

        int value = (currentPosition + addition) % currentSymbols.length;
        if (value < 0) {
            value += currentSymbols.length;
        }
        return value;

    }
}
