package no.ntnu.stud.avikeyb.gui.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import no.ntnu.stud.avikeyb.backend.Symbol;

/**
 * Helper class that loads layouts from an xml layout file
 * <p>
 * It this is useful when the layout is using the Symbol enum. With this helper class views can
 * be loaded by instances of the symbol enum. This only works if the xml layout defines the ids
 * if the symbol views in the following format: "sym_"+ lower_case_symbol_enum_name
 */
public class LayoutLoader {

    private Activity activity;
    private ViewGroup layout;

    public LayoutLoader(Activity activity, int layoutResourceId) {
        this.activity = activity;
        layout = (ViewGroup) activity.getLayoutInflater().inflate(layoutResourceId, null);
    }


    /**
     * Returns the loaded layout
     *
     * @return the layout instance
     */
    public View getLayout() {
        return layout;
    }

    /**
     * Returns true if the layout has an element with an id matching the given symbol
     *
     * @param symbol the symbol to look for in the layout
     * @return true if the symbol exists in the layout
     */
    public boolean hasSymbol(Symbol symbol) {
        return getResourceId(symbol) != 0 && getViewForSymbol(symbol) != null;
    }


    /**
     * Returns the view for the given symbol
     *
     * @param symbol the symbol to get
     * @return the view for the symbol
     */
    public View getViewForSymbol(Symbol symbol) {
        int resId = getResourceId(symbol);
        return layout.findViewById(resId);
    }

    /**
     * Return the view for the given Id
     *
     * @param id the id to get
     * @return the view for the id
     */
    public View getViewById(int id){
        return layout.findViewById(id);
    }

    // Returns the resource id for the given symbol
    private int getResourceId(Symbol symbol) {
        return activity.getResources().getIdentifier(
                "sym_" + symbol.name().toLowerCase(), "id", activity.getPackageName());
    }
}
