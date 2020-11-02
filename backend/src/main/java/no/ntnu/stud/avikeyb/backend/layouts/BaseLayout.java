package no.ntnu.stud.avikeyb.backend.layouts;

import java.util.ArrayList;
import java.util.List;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Layout;
import no.ntnu.stud.avikeyb.backend.layouts.util.LayoutState;

/**
 * Base implementation that implement the layout listeners
 */
public abstract class BaseLayout implements Layout {

    private List<LayoutListener> listeners;

    public BaseLayout() {
        listeners = new ArrayList<>();
    }



    @Override
    public void sendInputSignal(InputType input) {     // Implements the input interface
        onStep(input);
    }

    @Override
    public void addLayoutListener(LayoutListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeLayoutListener(LayoutListener listener) {
        listeners.remove(listener);
    }

    @Override
    public LayoutState getCurrentLayoutInternalState() {
        return new LayoutState(); // default empty state
    }

    /**
     * Notify the registered listeners that the layout has changed.
     * <p>
     * Listeners can be used by e.g. gui code to update the gui when the layout state changes
     * <p>
     * The layout implementations must call this every time the internal state changes
     */
    protected void notifyLayoutListeners() {
        for (Layout.LayoutListener listener : listeners) {
            listener.onLayoutChanged();
        }
    }

    /**
     * Called when a input signal of the specified type is detected
     *
     * @param input the input signal type that was registered
     */
    protected abstract void onStep(InputType input);
}
