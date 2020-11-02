package no.ntnu.stud.avikeyb.backend;

import no.ntnu.stud.avikeyb.backend.layouts.util.LayoutState;

/**
 * Interface representing a keyboard layout
 */
public interface Layout extends InputInterface {

    /**
     * A layout listener can be used to listen for changes in the layout state.
     * <p>
     * This can be useful to update e.g. a GUI when the layout state changes
     */
    interface LayoutListener {
        /**
         * Called when the layout state has changed
         */
        void onLayoutChanged();
    }

    /**
     * Add a listener to the layout
     *
     * @param listener the listener to add to the layout
     */
    void addLayoutListener(LayoutListener listener);

    /**
     * Removes a listener from the layout
     *
     * @param listener the listener to remove to the layout
     */
    void removeLayoutListener(LayoutListener listener);

    /**
     * Returns the current state of the layout
     *
     * @return a state object
     */
    LayoutState getCurrentLayoutInternalState();
}
