package no.ntnu.stud.avikeyb.gui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

import no.ntnu.stud.avikeyb.R;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.layouts.SimpleExampleLayout;
import no.ntnu.stud.avikeyb.gui.utils.LayoutLoader;

/**
 * Created by pitmairen on 09/02/2017.
 */

public class SimpleExampleLayoutGUI extends LayoutGUI {

    private Activity activity;

    // Store a reference to all the symbol for easy access when updating the gui
    private SimpleExampleLayout layout;
    private HashMap<Symbol, View> symbolViewMap = new HashMap<>();

    public SimpleExampleLayoutGUI(Activity activity, SimpleExampleLayout layout) {
        super();

        this.layout = layout;
        this.activity = activity;
    }


    public ViewGroup buildGUI() {

        LayoutLoader loader = new LayoutLoader(activity, R.layout.layout_simple_example);
        for (Symbol symbol : layout.getSymbols()) {
            if (loader.hasSymbol(symbol)) {
                TextView view = (TextView) loader.getViewForSymbol(symbol);
                view.setText(symbol.getContent());
                symbolViewMap.put(symbol, view);
            }
        }
        return (ViewGroup) loader.getLayout();
    }


    public void updateGUI() {

        // Highlight the selected symbol
        int current = layout.getCurrentPosition();
        int index = 0;

        for (Symbol symbol : layout.getSymbols()) {
            if (symbolViewMap.containsKey(symbol)) {
                if (current == index) {
                    symbolViewMap.get(symbol).setBackgroundColor(Color.GREEN);
                } else {
                    symbolViewMap.get(symbol).setBackgroundColor(Color.GRAY);
                }
            }
            index++;
        }
    }

}
