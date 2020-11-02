package no.ntnu.stud.avikeyb.backend.dictionary;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by pitmairen on 27/02/2017.
 */

public class StandardTestsBinaryDictionary extends DictionaryTester {
    @Override
    protected DictionaryHandler createDictionary(List<DictionaryEntry> entries) {
        // The binary dictionary expects the entries to be sorted alphabetically
        Collections.sort(entries, new Comparator<DictionaryEntry>() {
            @Override
            public int compare(DictionaryEntry e1, DictionaryEntry e2) {
                return e1.getWord().compareTo(e2.getWord());
            }
        });
        return new DictionaryHandler(entries);
    }
}
