package no.ntnu.stud.avikeyb.gui.core;

import java.util.Arrays;
import java.util.List;

import no.ntnu.stud.avikeyb.backend.dictionary.Dictionary;

/**
 * Dummy dictionary that always returns the same suggestions
 */
public class DummyDictionary implements Dictionary {

    @Override
    public List<String> getSuggestionsStartingWith(String query) {
        return Arrays.asList(query, "test1", "test2");
    }

    @Override
    public void updateWordUsage(String string) {
    }

}
