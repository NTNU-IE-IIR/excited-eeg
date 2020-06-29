package no.ntnu.stud.avikeyb.backend.layouts;

import static no.ntnu.stud.avikeyb.backend.Symbol.*;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The adaptive layout changes the order of the letters as the user types. After each typed letter,
 * the letters are placed in such a way that the letters with the highest probability
 * to follow the last typed letter are placed at the positions that requires the least amount
 * of steps to be selected in the layout.
 */
public class AdaptiveLayout extends BaseLayout implements LayoutWithSuggestions {

    /**
     * The possible layout states
     */
    public enum State {
        ROW_SELECTION,
        COLUMN_SELECTION,
        SUGGESTION_SELECTION,
    }

    // Currently a hard coded row length
    private static final int ROW_LENGTH = 6;

    private Keyboard keyboard;

    // Map from letter to optimal alphabet layout
    private HashMap<String, Symbol[]> optimalLayoutMap;

    private int currentRow;
    private int currentColumn;
    private int currentSuggestion;
    private Symbol[] currentAdaptiveLayout;
    private State currentState = State.ROW_SELECTION;
    private List<String> suggestions = new ArrayList<>();


    public AdaptiveLayout(Keyboard keyboard) {
        this.keyboard = keyboard;
        // Create the optimal layout map and reset to set the current layout based on the
        // keyboard's current output buffer.
        optimalLayoutMap = createComputedLayoutMap();
        reset();
    }

    @Override
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = new ArrayList<>(suggestions.subList(0, Math.min(6, suggestions.size())));
        this.currentSuggestion = -1; // 0
        if (currentState == State.SUGGESTION_SELECTION) {
            reset(); // If the suggestions change while we are in suggestions mode we have to reset to prevent crashes.
        }
        notifyLayoutListeners();
    }

    /**
     * Returns the current internal state of the layout
     *
     * @return the state of the layout
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Returns the symbols in the layout.
     * <p>
     * The order of the symbols will change after each typed letter. The order of the symbols
     * is the order that the implementation thinks is the optimal order based on the last typed
     * character.
     *
     * @return an array of symbols
     */
    public Symbol[] getSymbols() {
        return currentAdaptiveLayout;
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
     * Retuens the current active column in the layout
     *
     * @return the current active column index
     */
    public int getCurrentColumn() {
        return currentColumn;
    }


    /**
     * Returns the row length used in the layout
     *
     * @return the length of the rows
     */
    public int getRowSize() {
        return ROW_LENGTH;
    }

    public String getSuggestion() {
        if (currentSuggestion < suggestions.size()) {
            return suggestions.get(currentSuggestion);
        }
        return "";
    }

    public int getCurrentSuggestion() {
        if (currentSuggestion < suggestions.size()) {
            return currentSuggestion;
        }
        return 0;
    }


    public List<String> getSuggestions() {
        return suggestions;
    }

    /**
     * Handles positional logic for adaptive layout
     * @param input the input signal type that was registered
     */
    @Override
    protected void onStep(InputType input) {

        switch (currentState) {
            case ROW_SELECTION:
                stepInRowSelectionMode(input);
                break;
            case COLUMN_SELECTION:
                stepInColumnSelectionMode(input);
                break;
            case SUGGESTION_SELECTION:
                stepInSuggestionsMode(input);
                break;
        }

        notifyLayoutListeners();
    }

    /**
     * Changes the active row or selects it depending on input
     * @param input either INPUT1 for changing active row or INPUT2 to select it.
     */
    private void stepInRowSelectionMode(InputType input) {

        // Currently input 1 is used for moving to the next row and then input 2
        // is used to select the current row and switch to column selection mode.

        switch (input) {
            case INPUT1:
                // wrap around to the first row when the end is reached
                currentRow = (currentRow + 1) % getTotalRowCount();
                break;
            case INPUT2:
                currentState = State.COLUMN_SELECTION;
                break;
        }
    }

    /**
     * Changes the active column which contains a symbol or selects it depending on input
     * @param input either INPUT1 for changing active column or INPUT2 to select symbol
     */
    private void stepInColumnSelectionMode(InputType input) {

        switch (input) {
            case INPUT1:
                // wrap around to the first symbol in the row when the end is reached
                currentColumn = (currentColumn + 1) % getCurrentRowLength();
                break;
            case INPUT2:
                selectCurrentSymbol();
                break;
        }
    }

    /**
     * Changes the active suggestion or selects it depending on input
     * @param input either INPUT1 for changing active suggestion or INPUT2 to select suggestion
     */
    private void stepInSuggestionsMode(InputType input) {
        switch (input) {
            case INPUT1:
                if (getSuggestions().size() > 0) {
                    currentSuggestion = (currentSuggestion + 1) % suggestions.size();
                }
                break;
            case INPUT2:
                selectCurrentSuggestion();
                reset();
                break;
        }
    }



    /**
     * Select the symbol at current location
     */
    private void selectCurrentSymbol() {
        Symbol current = currentAdaptiveLayout[currentRow * ROW_LENGTH + currentColumn];
        if (current == Symbol.SEND) {
            keyboard.sendCurrentBuffer();
            reset();
        } else if (current == Symbol.DICTIONARY) {
            if (!suggestions.isEmpty()) {
                currentSuggestion++;
                currentState = State.SUGGESTION_SELECTION;
            }
        } else if(current == Symbol.CORRECT_WORD){
            keyboard.deleteLastCharacter();
            reset();
            //TODO consider remaining in position instead of returning to start
        } else if(current == Symbol.DELETE_WORD){
            keyboard.deleteLastWord();
            reset();
            //TODO consider remaining in position instead of returning to start
        } else if(current == Symbol.SETTING){
            keyboard.requestChangeSettings();
            reset();
        }
        else {
            keyboard.addToCurrentBuffer(current.getContent());
            reset();
        }
    }

    /**
     * Selects the suggestion at the current suggestion index
     */
    private void selectCurrentSuggestion() {
        String suggestion = suggestions.get(currentSuggestion);
        suggestion = suggestion.substring(keyboard.getCurrentWord().length());
        keyboard.addToCurrentBuffer(suggestion + Symbol.SPACE.getContent());
        reset();
    }



    /**
     * Reset the internal state, back to row selection state.
     */
    private void reset() {
        currentColumn = 0;
        currentRow = 0;
        currentSuggestion = -1; // 0
        currentState = State.ROW_SELECTION;
        currentAdaptiveLayout = getNextOptimalLayout(getLastTypedLetter());
    }

    /**
     * Returns the total number of rows in the layout
     * @return total amount of rows
     */
    private int getTotalRowCount() {
        // It has to be the ceiling because a partially filled row will count as a complete row.
        return (int) Math.ceil(currentAdaptiveLayout.length / (double) ROW_LENGTH);
    }


    /**
     * The number of columns in the current active row
     * @return columns in current row
     */
    private int getCurrentRowLength() {
        // The row length will be either the ROW_LENGTH or if we are at the last row in the layout
        // and this row is not completely filled, the length will be the total number of symbols in
        // in the layout minus the number of symbols in the preceding rows.
        return Math.min(ROW_LENGTH, currentAdaptiveLayout.length - currentRow * ROW_LENGTH);
    }

    /**
     * Returns the last letter in the keyboard output buffer
     * @return last typed letter
     */
    private String getLastTypedLetter() {
        String buffer = keyboard.getCurrentBuffer();
        if (buffer.length() > 0) {
            return Character.toString(buffer.charAt(buffer.length() - 1));
        }
        return "";
    }

    /**
     * Returns the optimal layout that should be used after the last letter typed
     * @param lastLetter The letter that was last typed
     * @return Returns a layout for the last typed letter
     */
    private Symbol[] getNextOptimalLayout(String lastLetter) {
        Symbol[] layout = optimalLayoutMap.get(lastLetter);
        if (layout != null) {
            return layout;
        }
        // Default to the layout coming after a space
        return optimalLayoutMap.get(" ");
    }

    /**
     * Maps each the english letters and space to a corresponding layout that should be used after the corresponding
     * letter has been typed. Each chunk of 6 characters represents a row in the layout.
     * @return HashMap containing the layout structures
     */
    private HashMap<String, Symbol[]> createComputedLayoutMap() {


        HashMap<String, Symbol[]> map = new HashMap<>();

        map.put(" ", new Symbol[]{T, A, I, C, F, L, O, S, B, M, N, Y, W, P, D, G, J, X, H, R, U, K, Z, QUESTION_MARK, E, V, Q, SPACE, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("a", new Symbol[]{N, R, L, SPACE, P, K, T, S, I, G, V, X, C, M, B, W, Z, J, D, Y, F, A, O, QUESTION_MARK, U, H, E, Q, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("b", new Symbol[]{E, A, O, I, Y, N, L, R, SPACE, T, D, H, U, S, J, M, F, X, B, C, P, G, Q, QUESTION_MARK, V, W, K, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("c", new Symbol[]{O, H, A, I, SPACE, M, E, K, R, C, P, F, T, U, Y, N, W, X, L, S, D, B, J, QUESTION_MARK, Q, G, V, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("d", new Symbol[]{SPACE, E, A, R, L, V, I, O, U, G, F, H, S, Y, N, J, P, Q, D, M, C, B, X, QUESTION_MARK, W, T, K, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("e", new Symbol[]{SPACE, R, D, L, M, F, S, N, T, P, G, B, A, C, V, W, H, U, E, X, I, Q, Z, QUESTION_MARK, Y, O, K, J, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("f", new Symbol[]{I, E, A, U, Y, N, O, R, L, S, H, G, F, SPACE, C, D, V, J, T, W, M, K, Q, QUESTION_MARK, B, P, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("g", new Symbol[]{SPACE, E, R, O, G, F, H, I, U, N, D, P, A, S, Y, W, V, J, L, T, B, K, Q, QUESTION_MARK, M, C, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("h", new Symbol[]{E, A, SPACE, U, N, W, O, I, R, L, F, P, T, Y, S, D, G, X, H, M, C, V, J, QUESTION_MARK, B, Q, K, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("i", new Symbol[]{N, S, C, L, G, SPACE, T, O, A, V, B, Q, E, D, M, Z, U, H, R, P, K, I, W, QUESTION_MARK, F, X, J, Y, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("j", new Symbol[]{E, A, U, T, H, F, O, I, R, L, P, B, SPACE, N, D, G, V, J, S, C, W, K, Q, QUESTION_MARK, M, Y, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("k", new Symbol[]{E, SPACE, S, N, U, P, I, Y, L, T, G, C, A, O, F, W, B, J, R, D, H, V, Q, QUESTION_MARK, M, K, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("l", new Symbol[]{E, I, L, Y, T, C, SPACE, A, S, F, P, W, O, U, V, B, G, J, D, K, R, H, Q, QUESTION_MARK, M, N, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("m", new Symbol[]{A, E, O, M, Y, T, I, SPACE, B, N, L, G, P, U, C, K, W, J, S, F, H, V, Q, QUESTION_MARK, R, D, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("n", new Symbol[]{G, SPACE, E, I, N, F, T, D, A, K, L, M, S, C, Y, B, Z, Q, O, V, J, R, W, QUESTION_MARK, U, H, P, X, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("o", new Symbol[]{N, R, L, T, P, K, U, M, S, C, A, Y, O, W, D, I, E, Z, SPACE, V, G, H, J, QUESTION_MARK, B, F, X, Q, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("p", new Symbol[]{E, R, O, SPACE, S, K, A, I, P, T, C, G, L, U, Y, F, W, J, H, M, N, V, Q, QUESTION_MARK, D, B, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("q", new Symbol[]{U, SPACE, T, I, H, F, E, A, N, L, P, B, O, S, D, G, V, J, R, C, W, K, Q, QUESTION_MARK, M, Y, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("r", new Symbol[]{E, SPACE, I, T, U, C, A, O, R, N, K, F, S, Y, M, V, W, X, D, G, B, H, Q, QUESTION_MARK, L, P, J, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("s", new Symbol[]{SPACE, T, I, O, C, K, E, S, U, L, N, G, H, A, Y, F, B, J, P, M, D, R, X, QUESTION_MARK, W, Q, V, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("t", new Symbol[]{SPACE, E, A, O, Y, N, I, H, S, L, F, G, R, T, C, B, V, X, U, M, Z, P, J, QUESTION_MARK, W, D, K, Q, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("u", new Symbol[]{R, N, L, C, A, SPACE, S, T, E, D, H, V, M, G, P, Y, K, J, I, B, Z, X, Q, QUESTION_MARK, F, O, U, W, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("v", new Symbol[]{E, I, O, U, L, C, A, SPACE, V, T, M, W, Y, S, N, F, B, J, R, H, P, K, Q, QUESTION_MARK, D, G, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("w", new Symbol[]{A, E, SPACE, N, D, K, I, H, S, W, T, P, O, R, B, M, G, J, L, F, U, V, Q, QUESTION_MARK, Y, C, X, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("x", new Symbol[]{P, I, T, C, S, R, SPACE, E, U, H, L, G, A, Y, W, D, B, X, O, Q, M, V, J, QUESTION_MARK, N, F, K, Z, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("y", new Symbol[]{SPACE, S, I, L, N, B, E, A, C, T, U, K, O, M, P, G, Z, X, D, R, F, V, J, QUESTION_MARK, W, H, Y, Q, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        map.put("z", new Symbol[]{E, A, I, Y, G, H, SPACE, O, S, V, D, P, Z, L, T, C, W, X, U, N, M, B, J, QUESTION_MARK, R, F, K, Q, COMMA, EXCLAMATION_MARK, DICTIONARY, CORRECT_WORD, DELETE_WORD, PERIOD, SETTING, SEND});
        return map;
    }

}
