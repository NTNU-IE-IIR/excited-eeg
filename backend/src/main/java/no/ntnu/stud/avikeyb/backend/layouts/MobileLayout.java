package no.ntnu.stud.avikeyb.backend.layouts;


import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.core.BackendLogger;
import no.ntnu.stud.avikeyb.backend.dictionary.DictionaryEntry;
import no.ntnu.stud.avikeyb.backend.dictionary.LinearEliminationDictionaryHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Handles mobile layout logic.
 * <p>
 * Created by Tor-Martin Holen on 21-Feb-17.
 */

public class MobileLayout extends BaseLayout {

    private int[] stepIndices;
    private Symbol[] symbols;
    private Keyboard keyboard;

    private State state = State.SELECT_ROW;
    private Mode mode = Mode.TILE_SELECTION_MODE;

    private ArrayList<Symbol> markedSymbols = new ArrayList<>();
    private int[] location = new int[]{-1, -1, -1, -1};
    private List<String> suggestions;
    private int nSuggestions = 20;

    private LinearEliminationDictionaryHandler dictionary;

    private List<Integer> cachedRowsTileMode = new ArrayList<>();
    private List<Integer> cachedRowsLetterMode = new ArrayList<>();

    public enum State {
        SELECT_ROW,
        SELECT_COLUMN,
        SELECT_LETTER,
        SELECT_DICTIONARY
    }

    public enum Mode {
        TILE_SELECTION_MODE,
        LETTER_SELECTION_MODE
    }


    /**
     * Constructor for Front-End applications that want to connect to the Back-End.
     *
     * @param keyboard
     * @param dictionary
     */
    public MobileLayout(Keyboard keyboard, final LinearEliminationDictionaryHandler dictionary) {
        suggestions = new ArrayList<>();
        this.keyboard = keyboard;
        this.dictionary = dictionary;
        updateLayoutStructure();
        setDefaultSuggestions();
        onStep(InputType.INPUT1);
/*        dictionary.setDictionaryChangedListener(new LinearEliminationDictionaryHandler.DictionaryChangedListener() {
            @Override
            public void dictionaryChanged() {
                setDefaultSuggestions();
                notifyLayoutListeners();
                BackendLogger.log("DictionaryChanged Interface is called");
            }
        });*/
    }

    /**
     * Constructor used for tests ONLY
     *
     * @param keyboard
     * @param dictionary
     * @param entries
     */
    public MobileLayout(Keyboard keyboard, LinearEliminationDictionaryHandler dictionary, List<DictionaryEntry> entries) {
        suggestions = new ArrayList<>();
        this.keyboard = keyboard;
        dictionary.setDictionary(entries);

        this.dictionary = dictionary;
        updateLayoutStructure();
        setDefaultSuggestions();
        onStep(InputType.INPUT1);
    }

    /**
     * Updates the layout structures if dictionary is on or off.
     * <p>
     * Note: Do not change layout structure without updating onStep logic accordingly.
     * Updating is needed if functionality symbols (any symbol not written with a single letter) are placed in another
     * row in the symbols array in this method.
     * </p>
     */
    private void updateLayoutStructure() {
        switch (mode) {
            case TILE_SELECTION_MODE:
                symbols = new Symbol[]{
                        Symbol.E, Symbol.T, Symbol.A,
                        Symbol.O, Symbol.I, Symbol.N,
                        Symbol.CORRECT_WORD, Symbol.DELETE_WORD, Symbol.DELETION_DONE,

                        Symbol.S, Symbol.R, Symbol.H,
                        Symbol.L, Symbol.D, Symbol.C, Symbol.U,
                        Symbol.W, Symbol.Y, Symbol.B, Symbol.V,

                        Symbol.M, Symbol.F, Symbol.P, Symbol.G,
                        Symbol.K, Symbol.X, Symbol.J, Symbol.Q, Symbol.Z,
                        Symbol.MODE_TOGGLE, Symbol.SETTING,

                        Symbol.DICTIONARY,
                        Symbol.PERIOD, Symbol.COMMA, Symbol.QUESTION_MARK, Symbol.EXCLAMATION_MARK,
                        Symbol.SEND};

                stepIndices = new int[]{
                        0, 3, 6, 9,
                        12, 16, 20,
                        24, 29, 31,
                        32, 36, 37};
                break;
            case LETTER_SELECTION_MODE:
                symbols = new Symbol[]{
                        Symbol.E, Symbol.T, Symbol.I, Symbol.SPACE,
                        Symbol.A, Symbol.N, Symbol.L,
                        Symbol.CORRECT_WORD, Symbol.DELETE_WORD, Symbol.DELETION_DONE,

                        Symbol.O, Symbol.S, Symbol.D,
                        Symbol.R, Symbol.C, Symbol.P, Symbol.B,
                        Symbol.M, Symbol.W, Symbol.K, Symbol.J,

                        Symbol.H, Symbol.U, Symbol.G, Symbol.V,
                        Symbol.F, Symbol.Y, Symbol.X, Symbol.Q, Symbol.Z,
                        Symbol.MODE_TOGGLE, Symbol.SETTING,

                        Symbol.DICTIONARY,
                        Symbol.PERIOD, Symbol.COMMA, Symbol.QUESTION_MARK, Symbol.EXCLAMATION_MARK,
                        Symbol.SEND};

                stepIndices = new int[]{
                        0, 4, 7, 10,
                        13, 17, 21,
                        25, 30, 32,
                        33, 37, 38};
                break;
        }

    }

    @Override
    protected void onStep(InputType input) {
        switch (state) {
            case SELECT_ROW:
                cacheExecution();
                onStepRowMode(input);
                break;
            case SELECT_COLUMN:
                onStepColumnMode(input);
                break;
            case SELECT_LETTER:
                onStepLetterMode(input);
                break;
            case SELECT_DICTIONARY:
                onStepDictionaryMode(input);
                break;
        }

        notifyLayoutListeners();
    }

    /**
     * Checks the marked row's current elements whenever or not they are cached in the {@link LinearEliminationDictionaryHandler},
     * if not a thread is started to handle the caching.
     */
    private void cacheExecution() {
        switch (mode) {
            case TILE_SELECTION_MODE:
                if (!cachedRowsTileMode.contains(location[0])) {
                    final List<List<String>> rowPossibilities = getRowTiles();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (final List<String> list : rowPossibilities) {
                                dictionary.cacheInputTile(list);
                            }
                        }
                    }).start();
                    cachedRowsTileMode.add(location[0]);
                }
                break;
            case LETTER_SELECTION_MODE:
                if (!cachedRowsLetterMode.contains(location[0])) {
                    final List<String> markedSymbolStrings = getStringsFromMarkedSymbols();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dictionary.cacheInputSingleLetters(markedSymbolStrings);
                        }
                    }).start();
                    cachedRowsLetterMode.add(location[0]);
                    break;
                }
        }
    }

    /**
     * Obtains the content the current row's tiles.
     * Note this method will contain strings of symbols that aren't single letters, so these must be handled properly.
     *
     * @return A list containing a list of strings (tile).
     */
    private List<List<String>> getRowTiles() {
        List<List<String>> result = new ArrayList<>();
        List<Symbol> first = new ArrayList<>();
        while (!markedSymbols.equals(first)) {
            if (first.isEmpty()) {
                nextColumn();
                first = markedSymbols;
                result.add(getStringsFromMarkedSymbols());
                nextColumn();
            } else {
                nextColumn();
            }
            result.add(getStringsFromMarkedSymbols());
            /*logMarked();*/
        }
        location[1] = -1;
        return result;
    }

    /**
     * Handles the input logic for rows.
     *
     * @param input
     */
    private void onStepRowMode(InputType input) {
        switch (input) {
            case INPUT1: //Move
                nextRow();
                if (mode == Mode.LETTER_SELECTION_MODE && suggestions.size() == 0) {
                    setCurrentSuggestions();
                }
                break;
            case INPUT2: //Select
                changeStateColumnSelection();
                break;
        }

    }

    /**
     * Handles the input logic for columns.
     *
     * @param input
     */
    private void onStepColumnMode(InputType input) {
        switch (input) {
            case INPUT1:
                nextColumn();
                break;
            case INPUT2:
                state = State.SELECT_ROW;
                if (markedSymbols.contains(Symbol.SEND)) {
                    send();
                } else if (markedSymbols.contains(Symbol.DICTIONARY)) {
                    changeStateDictionarySelection();
                } else if (markedSymbols.contains(Symbol.DELETE_WORD)) {
                    redirectDeletionFunctionality();
                } else if (markedSymbols.contains(Symbol.PERIOD)) {
                    changeStateLetterSelection();
                } else if (markedSymbols.contains(Symbol.MODE_TOGGLE)) {
                    changeStateLetterSelection();
                } else if (mode == Mode.TILE_SELECTION_MODE) {
                    letterTilePressed();
                } else if (mode == Mode.LETTER_SELECTION_MODE) {
                    changeStateLetterSelection();
                }
                break;
        }
    }

    /**
     * Handles the input logic for letter selection.
     *
     * @param input
     */
    private void onStepLetterMode(InputType input) {
        switch (input) {
            case INPUT1:
                nextLetter();
                break;
            case INPUT2:
                if (markedSymbols.contains(Symbol.DELETE_WORD)) {
                    handleWordDeletion();
                } else if (markedSymbols.contains(Symbol.CORRECT_WORD)) {
                    handleWordCorrection();
                } else if (markedSymbols.contains(Symbol.DELETION_DONE)) {
                    changeStateRowSelection();
                } else if (markedSymbols.contains(Symbol.PERIOD) || markedSymbols.contains(Symbol.COMMA) || markedSymbols.contains(Symbol.EXCLAMATION_MARK) || markedSymbols.contains(Symbol.QUESTION_MARK)) {
                    handleWordSeparatingSymbols();
                } else if (markedSymbols.contains(Symbol.SPACE)) {
                    finishWordWithSpace();
                    setDefaultSuggestions();
                    state = State.SELECT_ROW;
                    reset();
                } else if (markedSymbols.contains(Symbol.MODE_TOGGLE)){
                    handleModeToggle();
                } else if (markedSymbols.contains(Symbol.SETTING)){
                    handleSettingsSelected();
                }
                else if (mode == Mode.LETTER_SELECTION_MODE) {
                    handleLetterSelected();
                }
                break;
        }
    }

    /**
     * Handles input logic for dictionary selection
     *
     * @param input
     */
    private void onStepDictionaryMode(InputType input) {
        switch (input) {
            case INPUT1:
                nextDictionaryRow();
                break;
            case INPUT2:
                finishWordWithDictionary();
                setDefaultSuggestions();
                state = State.SELECT_ROW;
                reset();
                break;
        }
    }

    /**
     * Deletion functionality should be handled in the SELECT_LETTER state, so the state changes to that if there is
     * something that can be deleted available (word history or keyboard buffer).
     */
    private void redirectDeletionFunctionality() {
        if (dictionary.hasWordHistory() || !keyboard.getCurrentBuffer().isEmpty()) { // We don't want the user to input punctuation symbols when no words has been entered
            changeStateLetterSelection();
        }
    }

    /**
     * Punctuation logic should be handled in the SELECT_LETTER state, so the state changes to that if there is
     * something written in the keyboard buffer.
     */
    private void redirectPunctuationFunctionality() {
        if (!keyboard.getCurrentBuffer().isEmpty()) { // We don't want the user to input punctuation symbols when no words has been entered
            changeStateLetterSelection();
        }
    }

    /**
     * Sends the keyboard buffer and reverts to default state
     */
    private void send() {
        keyboard.sendCurrentBuffer();
        dictionary.reset();
        setDefaultSuggestions();
        reset();
    }


    private void letterTilePressed() {
        dictionary.findValidSuggestions(getStringsFromMarkedSymbols(), true);
        setSuggestions(dictionary.getSuggestions(nSuggestions));
        reset();
    }

    private void handleLetterSelected() {
        String marked = getMarkedSymbols().get(0).getContent();
        dictionary.findValidSuggestions(Collections.singletonList(marked), false);

        setCurrentSuggestions();
        selectCurrentSymbols();
        changeStateRowSelection();
    }

    private void handleWordDeletion() {
        if (!keyboard.getCurrentBuffer().isEmpty() || dictionary.hasWordHistory()) {
            if (!dictionary.hasWordHistory()) {
                deleteLastWord();
                dictionary.previousWord();
            } else {
                if (mode == Mode.LETTER_SELECTION_MODE) {
                    deleteLastWord();
                }
            }
            dictionary.clearWordHistory();
            setDefaultSuggestions();
        }
    }

    private void handleWordCorrection() {
        if (dictionary.hasWordHistory()) {
            dictionary.removeLastWordHistoryElement();
            if (mode == Mode.LETTER_SELECTION_MODE) {
                sliceWordToHistoryLength();
            }
            if (!dictionary.hasWordHistory()) {
                setDefaultSuggestions();
            } else {
                setCurrentSuggestions();
            }

        } else if (!(dictionary.hasWordHistory() && keyboard.getCurrentBuffer().isEmpty())) {
            if (mode == Mode.TILE_SELECTION_MODE) {
                deleteLastWord();
            } else if (mode == Mode.LETTER_SELECTION_MODE) {
                fixWord();
            }

            dictionary.previousWord();
            if (!dictionary.hasWordHistory() && keyboard.getCurrentBuffer().isEmpty() || dictionary.isCurrentHistoryEntrySpecial()) {
                dictionary.clearWordHistory();
                setDefaultSuggestions();
            } else {
                setCurrentSuggestions();
            }

        }
    }

    private void deleteLastWord() {
        dictionary.previousWord();
        if (dictionary.isCurrentHistoryEntrySpecial()) {
            removeSpecial();
        } else {
            keyboard.deleteLastWord();
        }
        setDefaultSuggestions();
        dictionary.nextWord();

    }

    private void fixWord() {
        String currentText = keyboard.getCurrentBuffer();
        int textLength = currentText.length();
        if (textLength >= 2) {
            String lastTwoCharacters = currentText.substring(textLength - 2, textLength);
            if (lastTwoCharacters.matches("[.,!?][ ]")) {
                removeSpecial();
            } else {
                removeLastLetter();
            }
        } else {
            removeLastLetter();
        }
    }

    private void removeSpecial() {
        keyboard.deleteLastCharacter();
        keyboard.deleteLastCharacter();
        keyboard.addToCurrentBuffer(" ");
        dictionary.clearWordHistory();
    }

    private void sliceWordToHistoryLength() {
        int currentWordLength = keyboard.getCurrentWord().length();
        int currentExpectedLength = dictionary.getWordHistorySize();
        if (currentWordLength > currentExpectedLength) {
            int removals = currentWordLength - currentExpectedLength;
            for (int i = 0; i < removals; i++) {
                deleteLastCharacter();
            }
        } else if (currentExpectedLength > currentWordLength) {
            int removals = currentExpectedLength - currentWordLength;
            for (int i = 0; i < removals; i++) {
                dictionary.removeLastWordHistoryElement();
            }
        }
    }

    private void deleteLastCharacter() {
        keyboard.deleteLastCharacter();
    }

    public void handleModeToggle() {
        if (mode == Mode.TILE_SELECTION_MODE) {
            mode = Mode.LETTER_SELECTION_MODE;
        } else if (mode == Mode.LETTER_SELECTION_MODE) {
            mode = Mode.TILE_SELECTION_MODE;
            if (dictionary.hasWordHistory()) {
                deleteLastWord();
            }
        }
        dictionary.clearWordHistory();
        setDefaultSuggestions();
        updateLayoutStructure();
        changeStateRowSelection();
    }

    private void handleSettingsSelected() {
        dictionary.clearWordHistory();
        setDefaultSuggestions();
        changeStateRowSelection();
        keyboard.requestChangeSettings();
    }



    private void handleWordSeparatingSymbols() {
        /*dictionary.nextWord();*/
        dictionary.addSpecialCharacterHistoryEntry(getMarkedSymbols().get(0).getContent());
        setDefaultSuggestions();
        writeSymbol();
        changeStateRowSelection();
    }

    private void changeStateRowSelection() {
        state = State.SELECT_ROW;
        reset();
    }

    private void changeStateColumnSelection() {
        state = State.SELECT_COLUMN;
        nextColumn();
    }

    private void changeStateLetterSelection() {
        state = State.SELECT_LETTER;
        nextLetter();
    }

    private void changeStateDictionarySelection() {
        if (!suggestions.isEmpty()) {
            state = State.SELECT_DICTIONARY;
            softReset();
            nextDictionaryRow();
        } else {
            reset();
        }
    }

    public Mode getMode() {
        return mode;
    }

    public LinearEliminationDictionaryHandler getDictionary() {
        return dictionary;
    }

    /**
     * @return list of strings
     */
    private List<String> getStringsFromMarkedSymbols() {
        List<String> stringList = new ArrayList<>();
        for (Symbol sym : markedSymbols) {
            stringList.add(sym.getContent());
        }
        return stringList;
    }

    public void logMarked() {
        String result = "";
        for (Symbol sym : markedSymbols) {
            result += sym.getContent() + " ";
        }

        BackendLogger.log("Marked symbols - " + result + ", Number of marked - " + markedSymbols.size());

    }

    public void softReset() {
        location = new int[]{-1, -1, -1, -1};
        markedSymbols = new ArrayList<>();
    }

    /**
     * Capitalizes a word if it starts with an lowercase letter [a-z], returns original word if not.
     *
     * @param currentWord The word that should be capitalized;
     * @return string with word capitalized, if it starts with a letter.
     */
    private String capitalizeWord(String currentWord) {
        if (currentWord.length() > 0) {
            String capitalizedLetter = currentWord.substring(0, 1).toUpperCase();
            if (currentWord.substring(0, 1).matches("[a-z]")) {
                return currentWord.replaceFirst("[a-z]", capitalizedLetter);
            }
        }
        return currentWord;
    }


    /**
     * Capitalizes a word that should be added to a text, if deemed appropriate.
     * Should be called every time a word is added to current text, if not all appropriate cases won't be found.
     *
     * @param currentText the text to check, for occurrences where a capital letter should be present.
     * @param nextWord    the word to check, if it should be modified to have a capital letter
     * @return
     */
    private String capitalizationCheck(String currentText, String nextWord) {
        if (currentText.isEmpty()) {
            nextWord = capitalizeWord(nextWord);
        }

        //RegExp if end matches ". ", "! " or "? " nextWord should be capitalized.
        if (currentText.matches("^[A-Za-z,.!?'\"\\s]+[.!?][ ]$")) {
            nextWord = capitalizeWord(nextWord);
        }
        return nextWord;
    }

    private void removeLastLetter() {
        keyboard.deleteLastCharacter();
    }

    private void finishWordWithDictionary() {
        String currentText = keyboard.getCurrentBuffer();
        String dictionaryWord = getSuggestions().get(location[3]);

        dictionaryWord = capitalizationCheck(currentText, dictionaryWord);
        if (mode == Mode.TILE_SELECTION_MODE || dictionary.getWordHistorySize() == 0) {
            keyboard.addToCurrentBuffer(dictionaryWord + " ");
        } else if (mode == Mode.LETTER_SELECTION_MODE) {
            int remainingLetters = keyboard.getCurrentWord().length();
            keyboard.addToCurrentBuffer(dictionaryWord.substring(remainingLetters) + " ");
        }

        dictionary.nextWord();

    }

    private void finishWordWithSpace() {
        keyboard.addToCurrentBuffer(" ");
        dictionary.nextWord();
    }

    private void writeSymbol() {
        String keyboardInput = keyboard.getCurrentBuffer().trim();
        keyboard.clearCurrentBuffer();
        keyboard.addToCurrentBuffer(keyboardInput);
        selectCurrentSymbols();
        if (!markedSymbols.get(0).getContent().equals(" ")) {
            keyboard.addToCurrentBuffer(" ");
        }
    }

    /**
     * Handles positional logic for rows
     */
    private void nextRow() {
        location[0]++;

        if (location[0] >= 4) {
            location[0] = 0;
        }


        markedSymbols = new ArrayList<>();
        int lowerBound = stepIndices[location[0] * 3];
        int upperBound = stepIndices[location[0] * 3 + 3];
        //Log.d("MobLayout", "Bounds, Lower: " + lowerBound + " Upper: " + upperBound);
        for (int i = lowerBound; i < upperBound; i++) {
            addMarkedSymbol(i);
        }
        //logLocation();
    }

    /**
     * Handles positional logic for columns
     */
    private void nextColumn() {
        location[1]++;
        markedSymbols = new ArrayList<>();

        int[] bounds = new int[]{0, 0};
        if (stepIndices.length == location[0] * 3 + location[1] + 1) {
            location[1] = 0;
            bounds = updateColumnBounds(bounds);
        } else {
            bounds = updateColumnBounds(bounds);
        }

        if (bounds[0] == bounds[1] || location[1] >= 3) {
            location[1] = 0;
            bounds = updateColumnBounds(bounds);
        }
        for (int i = bounds[0]; i < bounds[1]; i++) {
            addMarkedSymbol(i);
        }
        //logLocation();
    }

    private void nextDictionaryRow() {
        location[3]++;
        if (location[3] >= suggestions.size() || location[3] >= nSuggestions) {
            location[3] = 0;
        }
    }

    private int[] updateColumnBounds(int[] bounds) {
        bounds[0] = stepIndices[location[0] * 3 + location[1]];
        bounds[1] = stepIndices[location[0] * 3 + location[1] + 1];
        return bounds;
    }


    /**
     * Handles positional logic for selecting letters
     */
    private void nextLetter() {
        location[2]++;

        int lowerBound = stepIndices[location[0] * 3 + location[1]];
        int upperBound = stepIndices[location[0] * 3 + location[1] + 1];
        int symbolsInGroup = upperBound - lowerBound;

        //Log.d("MobLayout", "Symbols in group: " + symbolsInGroup);
        //Log.d("MobLayout", "Bounds, Lower: " + lowerBound + " Upper: " + upperBound);
        if (location[2] >= symbolsInGroup) {
            location[2] = 0;
        }

        markedSymbols = new ArrayList<>();
        int index = stepIndices[location[0] * 3 + location[1]] + location[2];
        //Log.d("MobLayout", "Index: " + index);
        addMarkedSymbol(index);
        //logLocation();
    }

    public ArrayList<Symbol> getMarkedSymbols() {
        return markedSymbols;
    }

    /**
     * Sends the current symbol, only usable in state SELECT_LETTER
     */
    private void selectCurrentSymbols() {
        Symbol symbol = markedSymbols.get(0); // will only contain one symbol in SELECT_LETTER state.

        if (symbol == Symbol.SEND) {
            keyboard.sendCurrentBuffer();
        } else {
            String text = symbol.getContent();
            text = capitalizationCheck(keyboard.getCurrentBuffer(), text);
            keyboard.addToCurrentBuffer(text);
        }
    }

    private void addMarkedSymbol(int index) {
        try {
            markedSymbols.add(symbols[index]);
        } catch (Exception e) {

        }
    }

    public List<String> getHistory() {
        return dictionary.getHistory();
    }

    public void reset() {
        location = new int[]{-1, -1, -1, -1};
        markedSymbols = new ArrayList<>();
        nextRow();
    }

    public Symbol[] getSymbols() {
        return symbols;
    }

    private void logLocation() {
        BackendLogger.log("Position: " + location[0] + ", " + location[1] + ", " + location[2]);
        String markedSymbolsText = "";
        for (Symbol s : markedSymbols) {
            markedSymbolsText += s.getContent() + " ";
        }
        BackendLogger.log(markedSymbolsText);
    }


    public void setSuggestions(List<String> suggestions) {
        this.suggestions = new ArrayList<>();
        this.suggestions.addAll(suggestions);
    }

    private void setCurrentSuggestions() {
        setSuggestions(dictionary.getSuggestions(nSuggestions));
    }

    public void setDefaultSuggestions() {
        setSuggestions(dictionary.getDefaultSuggestion(nSuggestions));
    }

    public int getMarkedWord() {
        return location[3];
    }


    public List<String> getSuggestions() {
        return new ArrayList<>(suggestions);
    }

    public State getState() {
        return state;
    }

    public int getMaxPossibleSuggestions() {
        return nSuggestions;
    }

    public void setDictionaryList(List<DictionaryEntry> list) {
        dictionary.setDictionary(list);
    }


}
