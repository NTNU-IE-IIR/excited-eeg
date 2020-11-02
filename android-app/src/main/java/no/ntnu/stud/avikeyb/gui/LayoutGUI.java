package no.ntnu.stud.avikeyb.gui;

import android.view.View;
import android.view.ViewGroup;

import no.ntnu.stud.avikeyb.backend.Keyboard;

/**
 * Base class for gui layouts
 */
public abstract class LayoutGUI {

    protected ViewGroup layoutContainer;

    public LayoutGUI() {
    }

    public void setLayoutContainer(ViewGroup layoutContainer){
        this.layoutContainer = layoutContainer;
    }


    /**
     * Called whenever the layout must be updated to reflect any changes in the layout backend
     */
    public abstract void updateGUI();

    /**
     *
     */
    public void onLayoutActivated(){
        layoutContainer.removeAllViews();
        layoutContainer.addView(buildGUI());
    }

    /**
     * Builds and returns the keyboard layout view
     *
     * @return a view of the layout
     */
    protected  abstract View buildGUI();


}
