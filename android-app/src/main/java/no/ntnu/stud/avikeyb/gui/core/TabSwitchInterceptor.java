package no.ntnu.stud.avikeyb.gui.core;

import android.graphics.Color;
import android.support.design.widget.TabLayout;

import no.ntnu.stud.avikeyb.backend.InputInterface;
import no.ntnu.stud.avikeyb.backend.InputType;

/**
 * Used to implement tab switching when the user selects the settings button in the layout
 * <p>
 * The switcher work by wrapping the input interface of the keyboard and intercept the input signals
 * when the switcher is active. When the switcher is deactivated the signals are passed through
 * to the wrapped input interface.
 */
public class TabSwitchInterceptor {

    private TabLayout tabs;
    private boolean isActive = false;

    /**
     * @param tabLayout the tab layout that contains the layouts
     */
    public TabSwitchInterceptor(TabLayout tabLayout) {
        this.tabs = tabLayout;
    }

    /**
     * Activates the switcher
     */
    public void activate() {
        this.isActive = true;
        tabs.setBackgroundColor(Color.LTGRAY);
    }

    /**
     * Deactivates the switcher
     */
    public void deactivate() {
        this.isActive = false;
        tabs.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Wraps the provided input interface
     *
     * @param inputInterface the input interface to wrap
     * @return a new wrapped input interface
     */
    public InputInterface interceptInput(final InputInterface inputInterface) {
        return new InputInterface() {
            @Override
            public void sendInputSignal(InputType inputType) {
                if (!isActive) {
                    inputInterface.sendInputSignal(inputType);
                } else {
                    handleTabSwitching(inputType);
                }
            }
        };
    }


    private void handleTabSwitching(InputType inputType) {
        if (inputType == InputType.INPUT1) {
            int nextTab = (tabs.getSelectedTabPosition() + 1) % tabs.getTabCount();
            tabs.getTabAt(nextTab).select();
        } else {
            deactivate();
        }
    }


}
