package no.ntnu.stud.avikeyb.gui.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

/**
 * A extension of the ListView class.
 * Contains utilities for scrolling when the lists selected element exceeds or equals the visible
 * midpoint element.
 * Note: Touch input can't and shouldn't be used to navigate a list of this type.
 */
public class AutoScrollListView extends ListView {

    public AutoScrollListView(Context context) {
        super(context);
        setEnabled(false);
    }

    public AutoScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEnabled(false);
    }

    public AutoScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setEnabled(false);
    }


    public void smoothScrollToStart() {
        smoothScrollToPosition(0);
    }



    /**
     * Handles the scrolling on the ListView, so it scrolls when the active position is in the
     * middle of the ListView and scrolls the active position so it's the top visible entry or if
     * there is few entries left in the ListView to the highest possible position.
     *
     * @param position the layouts active position for the dictionary
     */
    public void smoothScrollAuto(int position) {
        int visibleMidpoint = (getFirstVisiblePosition() + getLastVisiblePosition()) / 2;
        if (position == 0) {
            smoothScrollToStart();
        } else if (position >= visibleMidpoint){
            smoothScrollToPositionFromTop(visibleMidpoint, 0);
        }
    }
}