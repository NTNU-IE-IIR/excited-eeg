package no.ntnu.stud.avikeyb.gui.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import no.ntnu.stud.avikeyb.R;

/**
 * Reusable selections view that lists the current suggestions and highlights the current active suggestion
 */
public class GenericSuggestions {

    public static abstract class SuggestionsState {
        /**
         * Returns true is the suggestion is active
         */
        public abstract boolean isActive(String suggestion);

        /**
         * Returns the suggestion at the given position
         */
        public abstract String getSuggestion(int position);

        /**
         * Returns the current number of suggestions
         */
        public abstract int suggestionCount();

        /**
         * Returns the background resource to use for the suggestions view items
         */
        public int backgroundResource() {
            return R.drawable.text_selection_colors;
        }

        /**
         * Returns the layout resource to use for the suggestions view items
         */
        public int layoutResource() {
            return R.layout.generic_suggestion_item;
        }

    }


    public static class Adapter extends RecyclerView.Adapter<ViewHolder> {


        private SuggestionsState state;

        public Adapter(SuggestionsState state) {
            super();
            this.state = state;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            android.view.View view = LayoutInflater.from(parent.getContext()).inflate(state.layoutResource(), parent, false);
            ViewHolder myViewHolder = new ViewHolder((TextView) view.findViewById(android.R.id.text1));
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String suggestion = state.getSuggestion(position);
            holder.item.setText(suggestion);
            holder.item.setBackgroundResource(state.backgroundResource());
            holder.item.setSelected(state.isActive(suggestion));
        }

        @Override
        public int getItemCount() {
            return state.suggestionCount();
        }

    }


    public static class View extends RecyclerView {

        public View(Context context) {
            super(context);
            setup();
        }

        public View(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            setup();
        }

        public View(Context context, @Nullable AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            setup();
        }


        private void setup() {
            setLayoutManager(new LinearLayoutManager(getContext()));
            setItemAnimator(new DefaultItemAnimator());
        }
    }


    // View holder for the recycler view adapter
    private static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item;

        public ViewHolder(TextView view) {
            super(view);
            view.setTypeface(null, Typeface.BOLD);
            this.item = view;
        }
    }

}
