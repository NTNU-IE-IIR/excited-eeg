package no.ntnu.stud.avikeyb.webapi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.stud.avikeyb.backend.Layout;
import no.ntnu.stud.avikeyb.backend.layouts.BinarySearchLayout;
import no.ntnu.stud.avikeyb.backend.layouts.util.LayoutState;

import java.io.StringWriter;
import java.util.List;


public class LayoutStateConverter {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    static {
        // Example of mixin usage
        jsonMapper.addMixIn(BinarySearchLayout.class, BinarySearchStateMixin.class);
    }


    public static String stateToJSON(LayoutState state) {
        StringWriter w = new StringWriter();
        try {
            jsonMapper.writeValue(w, state.getState());
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("Failed to convert the values to json");
        }
        return w.getBuffer().toString();
    }


    public static String layoutToJSON(Layout layout) {
        StringWriter w = new StringWriter();
        try {
            jsonMapper.writeValue(w, layout);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("Failed to convert the values to json");
        }
        return w.getBuffer().toString();
    }


    /**
     * Currently not used, but an example of how to use mixins to convert a layout to a json object
     */
    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public interface BinarySearchStateMixin {

        @JsonProperty
        List<Object> getRight();

        @JsonProperty
        List<Object> getLeft();

        @JsonProperty
        List<String> getSuggestions();
    }

}
