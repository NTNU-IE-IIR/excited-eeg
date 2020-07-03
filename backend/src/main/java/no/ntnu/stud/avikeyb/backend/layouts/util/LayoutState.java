package no.ntnu.stud.avikeyb.backend.layouts.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds data that represents the internal state of a layout. This can be used by ui implementations to
 * show a visual representation of the layout state.
 */
public class LayoutState {

    private Map<String, Object> stateData = new HashMap<>();

    public void add(String key, Object value){
        stateData.put(key, value);
    }

    public Map<String, Object> getState(){
        return stateData;
    }
}
