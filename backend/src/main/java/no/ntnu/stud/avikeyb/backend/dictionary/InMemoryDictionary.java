package no.ntnu.stud.avikeyb.backend.dictionary;

import java.util.List;

/**
 * @author Kristian Honningsvag.
 */
public interface InMemoryDictionary {

    /**
     * This method should set the dictionary.
     *
     * @param dictionary
     */
    public void setDictionary(List<DictionaryEntry> dictionary);

    /**
     * Should return the list of dictionary entries.
     *
     * @return List of dictionary entries.
     */
    public List<DictionaryEntry> getDictionary();

}
