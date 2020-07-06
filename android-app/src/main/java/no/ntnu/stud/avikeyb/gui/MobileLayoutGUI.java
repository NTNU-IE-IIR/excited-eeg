package no.ntnu.stud.avikeyb.gui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.ntnu.stud.avikeyb.R;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.dictionary.DictionaryEntry;
import no.ntnu.stud.avikeyb.backend.layouts.MobileLayout;
import no.ntnu.stud.avikeyb.gui.utils.AutoScrollListView;
import no.ntnu.stud.avikeyb.gui.utils.LayoutLoader;
import no.ntnu.stud.avikeyb.gui.utils.TextAdapter;

/**
 * Created by Tor-Martin Holen on 15-Feb-17.
 */

public class MobileLayoutGUI extends LayoutGUI {

    private MobileLayout layout;
    private Activity activity;
    private HashMap<Symbol, View> symbolViewMap = new HashMap<>();
    private ArrayList<Symbol> previouslyMarked = new ArrayList<>();
    private int layoutResource1;
    private int layoutResource2;

    private AutoScrollListView dictionaryList;
    private TextAdapter dictionaryListAdapter;
    private TextView  emptyDictionaryView;

    private TextAdapter historyListAdapter;
    private LayoutLoader loader;
    private MobileLayout.Mode previousLayoutState;


    public MobileLayoutGUI(Activity activity, MobileLayout layout, int layoutResource1, int layoutResource2) {
        super();
        this.activity = activity;
        this.layout = layout;
        this.layoutResource1 = layoutResource1;
        this.layoutResource2 = layoutResource2;
        /*layout.logMarked();*/
    }


    @Override
    protected View buildGUI() {
        //Defines which layout resource should be used, depending on layout mode
        if (layout.getMode() == MobileLayout.Mode.TILE_SELECTION_MODE) {
            loader = new LayoutLoader(activity, layoutResource1);
        } else if (layout.getMode() == MobileLayout.Mode.LETTER_SELECTION_MODE) {
            loader = new LayoutLoader(activity, layoutResource2);
        }


        //Initialises the dictionary list.
        dictionaryList = (AutoScrollListView) loader.getViewById(R.id.autolistview);
        dictionaryListAdapter = new TextAdapter(activity.getApplicationContext(), R.id.autolistview, new ArrayList<String>());
        dictionaryList.setAdapter(dictionaryListAdapter);

        emptyDictionaryView = (TextView) loader.getViewById(R.id.emptyInfo);

        //Initialises the history list.
        ListView historyList = (ListView) loader.getViewById(R.id.historylist);
        historyListAdapter = new TextAdapter(activity.getApplicationContext(), R.id.autolistview, new ArrayList<String>());
        historyList.setAdapter(historyListAdapter);
        historyList.setEnabled(false);

        //Sets text on the different layout TextViews using the loader created above.
        for (Symbol symbol : layout.getSymbols()) {
            if (symbol != null && loader.hasSymbol(symbol)) {
                TextView guiTextTile = (TextView) loader.getViewForSymbol(symbol);

                guiTextTile.setText(symbol.getContent());
                guiTextTile.setTextColor(Color.BLACK);
                guiTextTile.setBackgroundResource(R.drawable.text_selection_colors);

                if (symbol.equals(Symbol.DICTIONARY)) {
                    guiTextTile.setText(Symbol.DICTIONARY_UNICODE_SYMBOL.getContent());
                }
                symbolViewMap.put(symbol, guiTextTile);
            }
        }
        return loader.getLayout();
    }


    @Override
    public void updateGUI() {
        //Changes the layout mode and rebuilds the layout
        if (layout.getMode() != previousLayoutState) {
            onLayoutActivated();
            /*layout.logMarked();*/
        }
        previousLayoutState = layout.getMode();

        updateKeyboardPart();
        updateLists();
        emptyListMessage();
    }

    /**
     * Used to update the gui so the default marked elements are marked properly
     */
    public void firstUpdate() {
        updateGUI();
    }

    /**
     * Updates elements currently marked in the keyboard part
     */
    private void updateKeyboardPart() {
        ArrayList<Symbol> newlyMarked = new ArrayList<>(layout.getMarkedSymbols());
        for (Symbol symbol : previouslyMarked) {
            if (symbolViewMap.containsKey(symbol)) {
                symbolViewMap.get(symbol).setSelected(false);
            }
        }
        for (Symbol symbol : newlyMarked) {
            if (symbolViewMap.containsKey(symbol)) {
                symbolViewMap.get(symbol).setSelected(true);
            }
        }
        previouslyMarked = newlyMarked;
    }

    /**
     * Updates the dictionary and word history lists and handles proper dictionary navigation and scrolling.
     */
    private void updateLists() {
        if (layout.getMarkedWord() == -1) {
            historyListAdapter.update(layout.getHistory());
            dictionaryListAdapter.update(layout.getSuggestions());
            dictionaryListAdapter.setCurrentPosition(-1);
            dictionaryList.smoothScrollToStart();
        } else {
            int position = layout.getMarkedWord();
            dictionaryListAdapter.setCurrentPosition(position);
            dictionaryList.smoothScrollAuto(position);
        }
    }

    private void emptyListMessage(){
        if (layout.getSuggestions().isEmpty()) {
            emptyDictionaryView.setVisibility(View.VISIBLE);
        } else {
            emptyDictionaryView.setVisibility(View.GONE);
        }
    }
}



































