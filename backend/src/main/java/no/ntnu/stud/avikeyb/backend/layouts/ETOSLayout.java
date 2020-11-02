package no.ntnu.stud.avikeyb.backend.layouts;


import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.Symbols;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ingalill on 10/02/2017.
 */

public class ETOSLayout extends BaseLayout implements LayoutWithSuggestions {

    /*private static Symbol[] symbols = Symbols.merge(
            Symbols.build(Symbol.SEND),
            Symbols.etos(),
            Symbols.numbers(),
            Symbols.commonPunctuations()
    );*/

    private static Symbol[] symbols = {
            Symbol.SPACE, Symbol.E, Symbol.A, Symbol.N, Symbol.L, Symbol.F,
            Symbol.T, Symbol.O, Symbol.S, Symbol.D, Symbol.P, Symbol.B,
            Symbol.I, Symbol.R, Symbol.C, Symbol.G, Symbol.V, Symbol.J,
            Symbol.H, Symbol.U, Symbol.W, Symbol.K, Symbol.Q, Symbol.QUESTION_MARK,
            Symbol.M, Symbol.Y, Symbol.X, Symbol.Z, Symbol.COMMA, Symbol.EXCLAMATION_MARK,
            Symbol.DICTIONARY, Symbol.CORRECT_WORD, Symbol.DELETE_WORD, Symbol.PERIOD, Symbol.SETTING, Symbol.SEND
    };

    private static Symbol[] menu = Symbols.menuOptions();


    public enum State {
        SELECT_ROW,
        SELECT_COLUMN,
        SELECT_MENU,
        SELECT_DICTIONARY,
    }


    // the current position of the cursor in the dictionary
    private int currentDictionaryPosition = -1;
    // the current position of the cursor in the menu
    private int currentMenuPosition = -1;
    // The current row of the cursor in the layout.
    private State state = State.SELECT_ROW;
    private Keyboard keyboard;

    private List<String> dictionsuggestions = new ArrayList<>();


    public ETOSLayout(Keyboard keyboard) {
        this.keyboard = keyboard;

    }

    // The current row of the cursor
    private int currentRow = 0;
    // the current column of the cursor
    private int currentColumn = 0;
    private static final int ROW_LENGTH = 6;


    @Override
    public void setSuggestions(List<String> suggestions) {
        dictionsuggestions.clear();
        dictionsuggestions.addAll(suggestions);
        notifyLayoutListeners();
    }

    /**
     * Returns the current active row in the layot
     *
     * @return the current active row index
     */
    public int getCurrentRow() {
        return currentRow;
    }

    /**
     * Returns the row length used in the layout
     *
     * @return the length of the rows
     */
    public int getRowSize() {
        return ROW_LENGTH;
    }

    /**
     * Retuens the current active column in the layout
     *
     * @return the current active column index
     */
    public int getCurrentColumn() {
        return currentColumn;
    }

    // The number of columns in the current active row
    private int getCurrentRowLength() {
        // The row length will be either the ROW_LENGTH or if we are at the last row in the layout
        // and this row is not completely filled, the length will be the total number of symbols in
        // in the layout minus the number of symbols in the preceding rows.
        return Math.min(ROW_LENGTH, symbols.length - currentRow * ROW_LENGTH);
    }


    // Returns the total number of rows in the layout
    private int getTotalRowCount() {
        // It has to be the ceiling because a partially filled row will count as a complete row.
        return (int) Math.ceil(symbols.length / (double) ROW_LENGTH);
    }

    /**
     * Get the current internal state of the layout
     *
     * @return the state of the layout
     */
    public State getCurrentState() {
        return state;
    }

    public List<String> getSuggestions() {
        return dictionsuggestions;
    }


    /**
     * Get the current suggestion word
     *
     * @return return the current suggestion from its position.
     */
    public String getCurrentSuggestion() {
        if (currentDictionaryPosition < dictionsuggestions.size()) {
            return dictionsuggestions.get(currentDictionaryPosition);
        }
        return "";
    }


    /**
     * Returns the current active position in the layout
     *
     * @return the position of the current active symbol
     */
    public int getSymbolCount() {
        return symbols.length;
    }


    public int getCurrentMenuPosition() {
        return currentMenuPosition;
    }

    /**
     * Returns the current active position in the dictionary
     *
     * @return the position of the current active dictionary string.
     */
    public int getCurrentDictionaryPosition() {
        return currentDictionaryPosition;
    }

    public Symbol getCurrentMenu() {
        System.out.println("Current menu pos: " + currentMenuPosition);
        if (currentMenuPosition < menu.length) {
            return menu[currentMenuPosition];
        }
        System.out.println("Menu: " + menu[currentMenuPosition]);

        return null;
    }

    public Symbol getCurrentSymbol() {
        Symbol current = symbols[currentRow * ROW_LENGTH + currentColumn];
        return current;
    }

    /**
     * Returns all symbols in the layout
     *
     * @return the symbols in the layout
     */
    public Symbol[] getSymbols() {

        return symbols;
    }

    /**
     * Returns all the menu options in the layout
     *
     * @return the menu options in the layout
     */
    public Symbol[] getMenuOptions() {
        return menu;

    }

    @Override
    protected void onStep(InputType input) {

        switch (state) {
            case SELECT_ROW:
                switch (input) {
                    case INPUT1: // move
                        nextRow();
                        break;
                    case INPUT2: // selects
                        state = State.SELECT_COLUMN;
                        break;
                }
                break;
            case SELECT_COLUMN:
                switch (input) {
                    case INPUT1: // move
                        nextColumn();
                        break;
                    case INPUT2: // selects

                        if (getCurrentSymbol().equals(Symbol.DICTIONARY) && getCurrentSymbol() != null) {
                            currentMenuPosition++; //todo, is this necessary?
                            currentDictionaryPosition++;
                            state = State.SELECT_DICTIONARY;
                        } else if (getCurrentSymbol().equals(Symbol.MENU)) {
                            currentMenuPosition++;
                            state = State.SELECT_MENU;
                        } else if(getCurrentSymbol().equals((Symbol.CORRECT_WORD))){
                            keyboard.deleteLastCharacter();
                            reset();
                            //TODO consider remaining in position instead of returning to start
                        } else if(getCurrentSymbol().equals((Symbol.DELETE_WORD))){
                            keyboard.deleteLastWord();
                            reset();
                            //TODO consider remaining in position instead of returning to start
                        } else if(getCurrentSymbol().equals(Symbol.SETTING)){
                            keyboard.requestChangeSettings();
                            reset();
                        }
                        else{
                            selectCurrentSymbol();
                            reset();
                        }
                        break;
                }
                break;

            case SELECT_DICTIONARY:
                switch (input) {
                    case INPUT1: // moves
                        nextDictionaryEntry();
                        break;
                    case INPUT2: // selects

                        if (getCurrentSuggestion() != null) {
                            selectSuggestion(getCurrentSuggestion());
                        }
                        reset();
                        break;
                }
                break;

            case SELECT_MENU:
                switch (input) {
                    case INPUT1:
                        nextMenuEntry();

                        break;
                    case INPUT2:
                        getCurrentMenu();
                        reset();
                        break;
                }
                break;
        }
        notifyLayoutListeners();
    }

    public void reset() {
        currentColumn = 0;
        currentRow = 0;
        currentDictionaryPosition = -1;
        currentMenuPosition = -1;
        state = State.SELECT_ROW;

    }

    /**
     * Moving cursor to the next row
     */
    public void nextRow() {
        currentRow = (currentRow + 1) % getTotalRowCount();
    }

    /**
     * Moving cursor to the next column.
     */
    public void nextColumn() {
        currentColumn = (currentColumn + 1) % getCurrentRowLength();
    }

    /**
     * If the suggestion is not null, then move the cursor to the next dictionary entry.
     */
    public void nextDictionaryEntry() {
        if (getSuggestions().size() > 0) {
            currentDictionaryPosition = (currentDictionaryPosition + 1) % getSuggestions().size();
        }
    }

    /**
     * If the menu is not null, move the cursor to the next menu entry.
     */
    public void nextMenuEntry() {
        if (menu.length > 0) {
            currentMenuPosition = (currentMenuPosition + 1) % menu.length;
            System.out.println("Current pos: " + currentMenuPosition);
        }

    }

    /**
     * Adds the suggestion to the output buffer.
     *
     * @param suggestion
     */
    private void selectSuggestion(String suggestion) {
        // Remove the characters that has already been written
        String sug = suggestion.substring(keyboard.getCurrentWord().length());
        keyboard.addToCurrentBuffer(sug + Symbol.SPACE.getContent());
    }

    private void selectCurrentSymbol() {
        Symbol current = symbols[currentRow * ROW_LENGTH + currentColumn];

        if (current == Symbol.SEND) {
            keyboard.sendCurrentBuffer();
        } else {
            keyboard.addToCurrentBuffer(current.getContent());
        }
    }

} // en of class
