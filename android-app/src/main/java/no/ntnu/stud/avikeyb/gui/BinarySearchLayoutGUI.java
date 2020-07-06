package no.ntnu.stud.avikeyb.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import no.ntnu.stud.avikeyb.R;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Symbol;
import no.ntnu.stud.avikeyb.backend.layouts.BinarySearchLayout;
import no.ntnu.stud.avikeyb.gui.utils.LayoutLoader;

/**
 * Created by pitmairen on 13/02/2017.
 */
public class BinarySearchLayoutGUI extends LayoutGUI {

    private BinarySearchLayout layout;
    private Activity activity;
    private HashMap<Symbol, View> symbolViewMap;
    private SuggestionsAdapter suggestionsAdapter;
    private RecyclerView suggestionsList;
    private int suggestionsListHeightCache = 0; // Used to calculate the height of the suggestion list items
    private TextView emptySuggestionsView;

    public BinarySearchLayoutGUI(Activity activity, BinarySearchLayout layout) {
        super();

        this.activity = activity;
        this.layout = layout;
        symbolViewMap = new HashMap<>();
        suggestionsAdapter = new SuggestionsAdapter();
    }

    @Override
    public ViewGroup buildGUI() {

        LayoutLoader loader = new LayoutLoader(activity, R.layout.layout_binsearch);
        for (Symbol symbol : layout.getSymbols()) {
            if (loader.hasSymbol(symbol)) {
                TextView view = (TextView) loader.getViewForSymbol(symbol);
                view.setText(getSymbolContent(symbol));
                symbolViewMap.put(symbol, view);
            }
        }

        emptySuggestionsView = (TextView) loader.getViewById(R.id.emptySuggestions);
        suggestionsList = (RecyclerView) loader.getViewById(R.id.suggestionsList);
        suggestionsList.setLayoutManager(new LinearLayoutManager(activity));
        suggestionsList.setItemAnimator(new DefaultItemAnimator());
        suggestionsList.setAdapter(suggestionsAdapter);

        // FIXME: If possible
        // Ugly hack to calculate the height of the items in the suggestions list to
        // make them match the height of the symbols in the table rows.
        calculateSuggestionsHeightWhenVisible();

        return (ViewGroup) loader.getLayout();
    }

    @Override
    public void updateGUI() {

        if(layout.getSuggestions().isEmpty()){
            emptySuggestionsView.setVisibility(View.VISIBLE);
            suggestionsList.setVisibility(View.GONE);
        }else{
            emptySuggestionsView.setVisibility(View.GONE);
            suggestionsList.setVisibility(View.VISIBLE);
            calculateSuggestionsHeightWhenVisible();
        }

        for (Map.Entry<Symbol, View> it : symbolViewMap.entrySet()) {

            if (layout.symbolIsActive(it.getKey())) {

                if (it.getValue() instanceof TextView) {
                    ((TextView) it.getValue()).setTextColor(ContextCompat.getColor(activity, R.color.binsearch_active_fg));
                }

                if (layout.symbolIsActiveLeft(it.getKey()))
                    it.getValue().setBackgroundColor(ContextCompat.getColor(activity, (R.color.binsearch_active_left_bg)));
                else
                    it.getValue().setBackgroundColor(ContextCompat.getColor(activity, R.color.binsearch_active_right_bg));
            } else {
                it.getValue().setBackgroundColor(ContextCompat.getColor(activity, R.color.binsearch_inactive_bg));
                if (it.getValue() instanceof TextView) {
                    ((TextView) it.getValue()).setTextColor(ContextCompat.getColor(activity, R.color.binsearch_inactive_fg));
                }
            }
        }
    }

    private String getSymbolContent(Symbol symbol){
        if(symbolContentOverrides.containsKey(symbol)){
            return symbolContentOverrides.get(symbol);
        }
        return symbol.getContent();
    }


    private void calculateSuggestionsHeightWhenVisible(){
        // The runnable is used to get the height of the suggestions view after it has been added
        // to the view and the height has been measured by the system.
        suggestionsList.post(new Runnable() {
            @Override
            public void run() {
                updateSuggestionViewHeightCache();
                suggestionsAdapter.notifyDataSetChanged();  // Notify that the item height have changed
            }
        });
    }

    private void updateSuggestionViewHeightCache(){
        int height = suggestionsList.getHeight();
        if(height != 0){
            suggestionsListHeightCache = height;
        }
    }

    // Make the suggestions item have the same height as the other symbols
    private void calculateAndSetSuggestionItemHeight(View view){
        updateSuggestionViewHeightCache();
        if(suggestionsListHeightCache != 0){
            view.getLayoutParams().height = (int) Math.floor(suggestionsListHeightCache / 7.0) - 6; // hardcoded calculation
        }
    }


    // Recycler view adapter
    private class SuggestionsAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.binsearch_suggestion_item, parent, false);
            ViewHolder myViewHolder = new ViewHolder(activity.getBaseContext(), (TextView) view.findViewById(android.R.id.text1));
            calculateAndSetSuggestionItemHeight(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String suggestion = layout.getSuggestions().get(position);

            if (layout.suggestionIsActive(suggestion)) {
                holder.item.setTextColor(ContextCompat.getColor(activity, R.color.binsearch_active_fg));
                if (layout.suggestionIsLeft(suggestion)) {
                    holder.item.setBackgroundColor(ContextCompat.getColor(activity, R.color.binsearch_active_left_bg));
                } else {
                    holder.item.setBackgroundColor(ContextCompat.getColor(activity, R.color.binsearch_active_right_bg));
                }
            } else {
                holder.item.setBackgroundColor(ContextCompat.getColor(activity, R.color.binsearch_inactive_bg));
                holder.item.setTextColor(ContextCompat.getColor(activity, R.color.binsearch_inactive_fg));
            }
            holder.item.setText(suggestion);
            calculateAndSetSuggestionItemHeight(holder.item);
        }

        @Override
        public int getItemCount() {
            return layout.getSuggestions().size();
        }
    }

    // View holder for the recycler view
    private static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item;

        public ViewHolder(Context context, TextView view) {
            super(view);
            if (Build.VERSION.SDK_INT < 23) {
                view.setTextAppearance(context,R.style.DefaultTextFormat);
            } else {
                view.setTextAppearance(R.style.DefaultTextFormat);
            }
            view.setAllCaps(false);

            this.item = view;
        }
    }

    private static Map<Symbol, String> symbolContentOverrides = new HashMap<>();
    static {
        symbolContentOverrides.put(Symbol.SPACE, "\u2423");
        symbolContentOverrides.put(Symbol.SETTING, "\u2699");
        symbolContentOverrides.put(Symbol.CLEAR_BUFFER, "Clear\nBuffer");
        //symbolContentOverrides.put(Symbol.DELETE_WORD, "\u226A");
    }
}
