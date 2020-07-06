package no.ntnu.stud.avikeyb.gui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import no.ntnu.stud.avikeyb.R;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.layouts.ETOSLayout;
import no.ntnu.stud.avikeyb.gui.utils.AutoScrollListView;
import no.ntnu.stud.avikeyb.gui.utils.MenuAdapter;
import no.ntnu.stud.avikeyb.gui.utils.LayoutLoader;
import no.ntnu.stud.avikeyb.gui.utils.TextAdapter;

/**
 * Created by ingalill on 10/02/2017.
 */

public class ETOSLayoutGUI extends LayoutGUI {

    private Activity activity;
    private ETOSLayout layout;

    private ArrayList<View> symbolViews = new ArrayList<>();

    private AutoScrollListView dictionaryList;

    private TextAdapter dictionaryAdapter;
    private LayoutLoader loader;
    private TextView emptySuggestionsView;


    public ETOSLayoutGUI(Activity activity, ETOSLayout layout) {
        super();
        this.layout = layout;
        this.activity = activity;
        loader = new LayoutLoader(activity, R.layout.layout_etos);
    }

    public ViewGroup buildGUI() {
        for (Symbol symbol : layout.getSymbols()) {
            if (symbol != null && loader.hasSymbol(symbol)) {
                TextView view = (TextView) loader.getViewForSymbol(symbol);
                view.setText(symbol.getContent());
                view.setTextColor(Color.BLACK);
                symbolViews.add(view);
            }
        }

        dictionaryList = (AutoScrollListView) loader.getViewById(R.id.listview);
        dictionaryAdapter = new TextAdapter(activity.getApplicationContext(), R.id.listview, new ArrayList<String>());


        dictionaryList.setAdapter(dictionaryAdapter);

        emptySuggestionsView = (TextView) loader.getViewById(R.id.emptySuggestions);

        return (ViewGroup) loader.getLayout();
    }


    public void updateGUI() {

        //Update dictionary
        if (layout.getCurrentDictionaryPosition() == -1 && layout.getSuggestions() != null) {
            dictionaryAdapter.update(layout.getSuggestions());
        } else {
            int position = layout.getCurrentDictionaryPosition();
            dictionaryAdapter.setCurrentPosition(position);
            dictionaryList.smoothScrollAuto(position);
        }

        //Hide dictionary when empty
        if (layout.getSuggestions().isEmpty()) {
            emptySuggestionsView.setVisibility(View.VISIBLE);
            dictionaryList.setVisibility(View.GONE);
        } else {
            emptySuggestionsView.setVisibility(View.GONE);
            dictionaryList.setVisibility(View.VISIBLE);
        }

        // Highlight the selected row or symbol
        Symbol[] currentLayout = layout.getSymbols();

        for (int i = 0; i < symbolViews.size(); i++) {
            TextView view = (TextView) symbolViews.get(i);

            // Because the layout of the symbols changes all the time we have to set the view
            // content each time we update.
            view.setText(currentLayout[i].getContent());

            int column = i % layout.getRowSize();
            int row = i / layout.getRowSize();
            view.setBackgroundResource(R.drawable.text_selection_colors);

            if (layout.getCurrentRow() == row) {
                view.setSelected(true);
                if (layout.getCurrentState() == ETOSLayout.State.SELECT_COLUMN && layout.getCurrentColumn() == column) {
                    view.setBackgroundResource(R.color.rowselected);
                }
            } else {
                view.setSelected(false); // Default non active background color
            }
        }
    }
}