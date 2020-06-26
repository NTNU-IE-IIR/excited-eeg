package no.ntnu.stud.avikeyb.gui.core;

import android.os.AsyncTask;

import java.util.List;

import no.ntnu.stud.avikeyb.backend.dictionary.Dictionary;

/**
 * Runs a query to the dictionary in a async task to prevent blocking of the GUI.
 */
public class AsyncSuggestions {

    /**
     * Callback to get the result from the async call
     */
    public interface ResultCallback {
        void onResult(List<String> suggestions);
    }


    // Remember the current task so it can be canceled if a new search is started
    // before the previous one is done
    private AsyncTask<Void, Void, List<String>> currentTask;
    private Dictionary dictionary;

    public AsyncSuggestions(Dictionary dictionary) {
        super();
        this.dictionary = dictionary;
    }


    public void findSuggestionsFor(final String prefix, final ResultCallback callback) {

        if (currentTask != null) {
            currentTask.cancel(false);
        }

        currentTask = new AsyncTask<Void, Void, List<String>>() {

            @Override
            protected List<String> doInBackground(Void... params) {
                return dictionary.getSuggestionsStartingWith(prefix);
            }

            @Override
            protected void onPostExecute(List<String> suggestions) {
                callback.onResult(suggestions);
                currentTask = null;
            }
        };
        currentTask.execute();
    }
}
