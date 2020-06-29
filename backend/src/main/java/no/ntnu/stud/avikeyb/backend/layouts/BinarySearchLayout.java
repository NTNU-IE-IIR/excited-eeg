package no.ntnu.stud.avikeyb.backend.layouts;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.Symbols;
import no.ntnu.stud.avikeyb.backend.layouts.util.LayoutState;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic binary search layout
 */
public class BinarySearchLayout extends BaseLayout implements LayoutWithSuggestions {


    // Only used for letting the ui know all the symbols in the layout
    private static Symbol[] symbols = Symbols.merge(
            Symbols.alphabet(),
            Symbols.build(Symbol.SPACE, Symbol.SEND, Symbol.BACKSPACE, Symbol.DELETE_WORD, Symbol.CLEAR_BUFFER, Symbol.SETTING),
            Symbols.numbers(),
            Symbols.commonPunctuations());


    private Keyboard keyboard;
    private List<String> suggestions = new ArrayList<>();

    private BinarySearchTreeDefinition.Node currentNode;

    public BinarySearchLayout(Keyboard keyboard) {
        this.keyboard = keyboard;
        reset();
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
     * Checks if a symbol is active
     *
     * @param symbol the symbol to check
     * @return true if the symbol is active
     */
    public boolean symbolIsActive(Symbol symbol) {
        return currentNode.contains(symbol);
    }

    /**
     * Checks if a symbol is active
     *
     * @param symbol the symbol to check
     * @return true if the symbol is active
     */
    public boolean symbolIsActiveLeft(Symbol symbol) {
        return currentNode.getLeft().contains(symbol);
    }

    /**
     * Checks if a symbol is active
     *
     * @param symbol the symbol to check
     * @return true if the symbol is active
     */
    public boolean symbolIsActiveRight(Symbol symbol) {
        return currentNode.getRight().contains(symbol);
    }


    /**
     * Check if a suggestion is active in the left bucket
     *
     * @param suggestion the suggestion to check
     * @return true if the suggestion is in the left bucket
     */
    public boolean suggestionIsLeft(String suggestion) {
        return currentNode.getLeft().contains(suggestion);
    }

    /**
     * Check if a suggestion is active in the right bucket
     *
     * @param suggestion the suggestion to check
     * @return true if the suggestion is in the right bucket
     */
    public boolean suggestionIsRight(String suggestion) {
        return currentNode.getRight().contains(suggestion);
    }

    /**
     * Check if a suggestion is active
     *
     * @param suggestion the suggestion to check
     * @return true if the suggestion is currently selectable
     */
    public boolean suggestionIsActive(String suggestion) {
        return currentNode.contains(suggestion);
    }


    /**
     * Returns the list of current suggested words
     *
     * @return a list of words
     */
    public List<String> getSuggestions() {
        return suggestions;
    }


    public LayoutState getCurrentLayoutInternalState() {
        LayoutState state = new LayoutState();
        state.add("left", currentNode.getLeft().getItems());
        state.add("right", currentNode.getRight().getItems());
        state.add("suggestions", suggestions);
        return state;
    }


    @Override
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = new ArrayList<>(suggestions.subList(0, Math.min(7, suggestions.size())));
        reset();
        notifyLayoutListeners();
    }

    @Override
    protected void onStep(InputType input) {

        // Select the correct side and split it into the new left and right side
        if (input == InputType.INPUT1) {
            selectLeft();
        } else if (input == InputType.INPUT2) {
            selectRight();
        }

        // Check if we have reached a final selection
        checkCompleted();
    }

    private void checkCompleted() {
        // If the current node is a single node we have reached the end of the selection process
        // so we select the current item
        if (currentNode.isSingle()) {
            selectCurrent();
            reset();
        }else{
            // We only need to notify the listeners when not selecting a symbol. When selecting a symbol
            // the listeners will be notified when the suggestions are updated.
            notifyLayoutListeners();
        }
    }

    // Get the current item and send it to the keyboard
    private void selectCurrent() {
        Object item = currentNode.getItem();
        if (item instanceof Symbol) {
            selectSymbol((Symbol)item);
        } else if (item instanceof String) {
            selectSuggestion((String) item);
        }
    }


    private void selectSymbol(Symbol symbol) {
        if (symbol == Symbol.SEND) {
            keyboard.sendCurrentBuffer();
        }
        else if((symbol == Symbol.DELETE_WORD || symbol == Symbol.BACKSPACE) && keyboard.getCurrentBuffer().isEmpty()){
            // Corner case where the keyboard buffer will not change because it is already empty, so we need to reset and
            // notify the listeners to update the ui if the user clicks one of the delete buttons when the buffer is empty.
            reset();
            notifyLayoutListeners();
        }
        else if(symbol == Symbol.BACKSPACE){
            keyboard.deleteLastCharacter();
        }
        else if(symbol == Symbol.DELETE_WORD){
            keyboard.deleteLastWord();
        }
        else if(symbol == Symbol.CLEAR_BUFFER){
            keyboard.clearCurrentBuffer();
        }
        else if(symbol == Symbol.SETTING){
            keyboard.requestChangeSettings();
            reset();
            notifyLayoutListeners();
        }
        else {
            keyboard.addToCurrentBuffer(symbol.getContent());
        }
    }

    private void selectSuggestion(String suggestion) {
        // Remove the characters that has already been written and add a space to the word before
        // adding it to the keyboard output buffer
        String sug = suggestion.substring(keyboard.getCurrentWord().length());
        keyboard.addToCurrentBuffer(sug + Symbol.SPACE.getContent());
    }

    private void selectLeft() {
        currentNode = currentNode.getLeft();
    }

    private void selectRight() {
        currentNode = currentNode.getRight();
    }

    // Rebuild the selection tree so that the user can start typing something new
    private void reset() {
        currentNode = BinarySearchTreeDefinition.buildBinarySearchLayoutTree(suggestions);
    }

}
