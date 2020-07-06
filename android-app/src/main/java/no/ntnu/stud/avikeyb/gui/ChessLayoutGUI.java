package no.ntnu.stud.avikeyb.gui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import no.ntnu.stud.avikeyb.R;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.gui.core.ChessLayout;
import no.ntnu.stud.avikeyb.gui.utils.LayoutLoader;

/**
 * Created by pitmairen on 04/04/2017.
 */
public class ChessLayoutGUI extends LayoutGUI {

    private Activity activity;

    private ChessLayout layout;
    private HashMap<Symbol, View> symbolViewMap = new HashMap<>();
    private View letterDivider;
    private View numberDivider;
    private View confirmDivider;


    public ChessLayoutGUI(Activity activity, ChessLayout layout) {
        super();
        this.layout = layout;
        this.activity = activity;
    }


    public ViewGroup buildGUI() {

        LayoutLoader loader = new LayoutLoader(activity, R.layout.layout_chess);
        for (Symbol symbol : layout.getSymbols()) {
            if (loader.hasSymbol(symbol)) {
                TextView view = (TextView) loader.getViewForSymbol(symbol);
                view.setText(getSymbolContent(symbol));
                symbolViewMap.put(symbol, view);
            }
        }
        letterDivider = loader.getViewById(R.id.chessInitial1);
        numberDivider = loader.getViewById(R.id.chessInitial2);
        confirmDivider = loader.getViewById(R.id.chessInitial3);

        return (ViewGroup) loader.getLayout();
    }


    public void updateGUI() {


        ChessLayout.State state = layout.getCurrentState();

        letterDivider.setBackgroundColor(Color.TRANSPARENT);
        numberDivider.setBackgroundColor(Color.TRANSPARENT);
        confirmDivider.setBackgroundColor(Color.TRANSPARENT);

        View currentDivider = letterDivider;
        int currentIndex = 0;

        if (state == ChessLayout.State.LETTER) {
            currentDivider = letterDivider;
        } else if (state == ChessLayout.State.NUMBER) {
            currentIndex += 8;
            currentDivider = numberDivider;
        } else if (state == ChessLayout.State.CONFIRM) {
            currentIndex += 16;
            currentDivider = confirmDivider;
        }

        if (!layout.isInitialSelection()) {
            currentIndex += layout.getCurrentPosition();
        } else {
            currentIndex = -1;
            currentDivider.setBackgroundColor(Color.GREEN);
        }

        int index = 0;
        for (Symbol symbol : layout.getSymbols()) {
            if (symbolViewMap.containsKey(symbol)) {
                if (currentIndex == index) {
                    symbolViewMap.get(symbol).setBackgroundResource(R.color.rowselected);
                } else if (isCurrentRow(index)) {
                    symbolViewMap.get(symbol).setBackgroundResource(R.color.selected);
                } else {
                    symbolViewMap.get(symbol).setBackgroundResource(R.color.background);
                }
            }
            index++;
        }
    }

    private boolean isCurrentRow(int index) {
        if (layout.getCurrentState() == ChessLayout.State.LETTER) {
            return index >= 0 && index < 8;
        } else if (layout.getCurrentState() == ChessLayout.State.NUMBER) {
            return index >= 8 && index < 16;
        } else if (layout.getCurrentState() == ChessLayout.State.CONFIRM) {
            return index >= 16;
        }
        return false;
    }


    private String getSymbolContent(Symbol symbol) {
        if (symbolContentOverrides.containsKey(symbol)) {
            return symbolContentOverrides.get(symbol);
        }
        return symbol.getContent();
    }

    private static Map<Symbol, String> symbolContentOverrides = new HashMap<>();

    static {
        symbolContentOverrides.put(Symbol.SEND, "SEND");
        symbolContentOverrides.put(Symbol.SETTING, "\u2699");
        symbolContentOverrides.put(Symbol.CLEAR_BUFFER, "Cancel");
    }
}
