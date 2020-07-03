package no.ntnu.stud.avikeyb.backend.layouts.util;

/**
 * Created by ingalill on 17/02/2017.
 */

import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Symbol;

/**
 * Handles positional logic for obtaining the symbols from layoutSymbols
 */
public class LayoutPosition {
    private int[] position;
    private Symbol[][][] layoutSymbols;

    public LayoutPosition(Symbol[][][] layoutSymbols) {
        position = new int[]{0, 0, 0};
        this.layoutSymbols = layoutSymbols;
    }

    /**
     * Moves to the next row, if there is no more rows it returns to start row.
     */
    public void nextRow() {
        position[0] += 1;
        if (position[0] >= layoutSymbols.length) {
            position[0] = 0;
        }
    }

    /**
     * Moves to the next column, if there is no more columns it returns to start column.
     */
    public void nextColumn() {
        position[1] += 1;
        if (position[1] >= layoutSymbols[position[0]].length) {
            position[1] = 0;
        }
    }

    /**
     * Moves to the next letter, if there is no more letters it returns to start letter.
     */
    public void nextLetter() {
        position[2] += 1;
        if (position[2] >= layoutSymbols[position[0]][position[1]].length) {
            position[2] = 0;
        }
    }

    /**
     * Resets the position, so the process of selecting a letter can start anew.
     */
    public void resetPosition()
    {
        position = new int[]{0, 0, 0};
    }

    public int[] getPosition() {

        return position;
    }

    /**
     * Sends the current symbol, only usable in state SELECT_LETTER
     */
    public void selectCurrentSymbol(Keyboard keyboard) {

        Symbol symbol = layoutSymbols[position[0]][position[1]][position[2]];

        if (symbol == Symbol.SEND) {
            keyboard.sendCurrentBuffer();
        } else {
            keyboard.addToCurrentBuffer(symbol.getContent());
        }
    }
}