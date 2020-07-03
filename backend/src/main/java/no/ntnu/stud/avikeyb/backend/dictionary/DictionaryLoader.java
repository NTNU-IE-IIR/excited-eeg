package no.ntnu.stud.avikeyb.backend.dictionary;

import java.util.List;

/**
 * Defines the interface used by the backend to load dictionaries into memory
 */
public interface DictionaryLoader {

    /**
     * Returns a list of dictionary entries
     *
     * @return a list of dictionary entries
     */
    List<DictionaryEntry> loadDictionary();
}
