package no.ntnu.stud.avikeyb.backend.layouts;

import java.util.List;

/**
 * Created by pitmairen on 3/12/17.
 */
public interface LayoutWithSuggestions {

    /**
     * Set the suggestions for the layout
     *
     * @param suggestions a list of suggestions
     */
    void setSuggestions(List<String> suggestions);

}
