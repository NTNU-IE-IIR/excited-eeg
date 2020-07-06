package no.ntnu.stud.avikeyb.gui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import no.ntnu.stud.avikeyb.R;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.Symbols;
import no.ntnu.stud.avikeyb.backend.layouts.AdaptiveLayout;

import no.ntnu.stud.avikeyb.gui.utils.AutoScrollListView;
import no.ntnu.stud.avikeyb.gui.utils.LayoutLoader;
import no.ntnu.stud.avikeyb.gui.utils.TextAdapter;


/**
 * Created by ingalill on 10/02/2017.
 */
public class AdaptiveLayoutGUI extends LayoutGUI {


    private Activity activity;
    private AdaptiveLayout layout;
    private TextAdapter dictionaryAdapter;
    private AutoScrollListView dictionaryList;
    private View previousViewSelected;
    private TextView emptySuggestionsView;


    // Store a reference to all the symbol for easy access when updating the gui
    private ArrayList<View> symbolViews = new ArrayList<>();

    public AdaptiveLayoutGUI(Activity activity, AdaptiveLayout layout) {
        super();

        this.layout = layout;
        this.activity = activity;
    }


    public View buildGUI() {

        // The orders of the symbols in this array must be in the same order as the symbols are
        // defined in the xml layout. This is because we need to know the position of each view
        // in the layout, as the views are reused when the adaptive layout changes by
        // replacing the content of the view with the content of the symbol in the corresponding
        // position in the backend layout.
        //
        // This means that as the layout changes the content of the views and the id of the views
        // will not match, but this does not matter as we are only interested in the position of
        // the views. The id of the views are only used initially to load the views in the correct
        // order.

        Symbol[] alphabetStart = Arrays.copyOfRange(Symbols.alphabet(), 0, 23);
        Symbol[] alphabetEnd = Arrays.copyOfRange(Symbols.alphabet(), 23, Symbols.alphabet().length);
        Symbol[] utilitySymbols = Symbols.build(
                Symbol.SPACE, Symbol.COMMA, Symbol.EXCLAMATION_MARK, Symbol.DICTIONARY,
                Symbol.CORRECT_WORD, Symbol.DELETE_WORD, Symbol.PERIOD,
                Symbol.SETTING, Symbol.SEND);


        Symbol[] symbols = Symbols.merge(alphabetStart, Symbols.build(Symbol.QUESTION_MARK), alphabetEnd, utilitySymbols);

        LayoutLoader loader = new LayoutLoader(activity, R.layout.layout_adaptive);
        for (Symbol symbol : symbols) {
            if (loader.hasSymbol(symbol)) {
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


        return loader.getLayout();
    }

    public void updateGUI() {

        if (layout.getCurrentState() == AdaptiveLayout.State.SUGGESTION_SELECTION) {
            updateSuggestions();
        } else {
            updateLayout();
        }
/*

            currentLayoutAsString();
*/

    }
/*

    public String[] currentLayoutAsString(){
        String[] resultLayout = {"","","","","",""};
        int index = 0;
        int additions = 0;
        for (Symbol sym:layout.getSymbols()) {
            if(additions < 6){
                additions++;
                try{
                    resultLayout[index] += sym.getContent() + " ";
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else {
                additions = 0;
                index++;
                try{
                    resultLayout[index] += sym.getContent() + " ";
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return resultLayout;
    }
*/

    private void updateLayout() {

        if (layout.getCurrentSuggestion() == -1 && layout.getSuggestions() != null) {
            dictionaryAdapter.update(layout.getSuggestions());
        }


        if (layout.getSuggestions().isEmpty()) {
            emptySuggestionsView.setVisibility(View.VISIBLE);
            dictionaryList.setVisibility(View.GONE);
        } else {
            emptySuggestionsView.setVisibility(View.GONE);
            dictionaryList.setVisibility(View.VISIBLE);
        }

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
                if (layout.getCurrentState() == AdaptiveLayout.State.COLUMN_SELECTION && layout.getCurrentColumn() == column) {
                    view.setBackgroundResource(R.color.rowselected);
                }
            } else {
                view.setSelected(false); // Default non active background color
            }
        }
    }

    private void updateSuggestions() {

        Symbol[] currentLayout = layout.getSymbols();
        for (int i = 0; i < symbolViews.size(); i++) {

            TextView view = (TextView) symbolViews.get(i);
            // Because the layout of the symbols changes all the time we have to set the view
            // content each time we update.
            view.setText(currentLayout[i].getContent());

            view.setSelected(false);
        }

        if (layout.getSuggestions() != null) {
            int position = layout.getCurrentSuggestion();
            dictionaryAdapter.setCurrentPosition(position);
            dictionaryList.smoothScrollAuto(position);
        }

    }


    //todo is this method necessary?
    private boolean checkIfSuggestionIsActive(String suggestion) {
        return layout.getCurrentState() == AdaptiveLayout.State.SUGGESTION_SELECTION
                && layout.getSuggestions().get(layout.getCurrentSuggestion()).equals(suggestion);
    }
}





