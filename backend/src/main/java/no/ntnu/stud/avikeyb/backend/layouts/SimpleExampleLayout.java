package no.ntnu.stud.avikeyb.backend.layouts;


import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.Symbols;
import no.ntnu.stud.avikeyb.backend.layouts.util.LayoutState;

/**
 * Simple layout for testing
 */
public class SimpleExampleLayout extends BaseLayout {

    // The symbols available in the layout.
    private static Symbol[] symbols = Symbols.merge(
            Symbols.build(Symbol.SEND),
            Symbols.alphabet(),
            Symbols.numbers(),
            Symbols.commonPunctuations());

    // The current position of the cursor in the layout
    private int currentPosition = 0;

    private Keyboard keyboard;

    public SimpleExampleLayout(Keyboard keyboard) {

        this.keyboard = keyboard;
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
     * Returns the current active position in the layout
     *
     * @return the position of the current active symbol
     */
    public int getCurrentPosition() {
        return currentPosition;
    }


    @Override
    public LayoutState getCurrentLayoutInternalState() {
        LayoutState state = new LayoutState();
        state.add("position", getCurrentPosition());
        return state;
    }

    @Override
    protected void onStep(InputType input) {

        switch (input) {
            case INPUT1: // Select the current symbol
                selectCurrentSymbol();
                reset();
                break;
            case INPUT2: // Step to the right in the layout
                currentPosition = (currentPosition + 1) % symbols.length;
                break;
        }

        notifyLayoutListeners();
    }


    private void selectCurrentSymbol() {

        Symbol current = symbols[currentPosition];

        if (current == Symbol.SEND) {
            keyboard.sendCurrentBuffer();
        } else {
            keyboard.addToCurrentBuffer(current.getContent());
        }
    }

    private void reset() {
        currentPosition = 0;
    }
}
