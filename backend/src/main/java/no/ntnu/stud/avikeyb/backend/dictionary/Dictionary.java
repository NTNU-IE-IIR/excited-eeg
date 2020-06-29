package no.ntnu.stud.avikeyb.backend.dictionary;

import java.util.List;

/**
 * Interface for dictionary lookup
 */
public interface Dictionary {


    /**
     * Should return a list of words that starts with the match string
     *
     * @param match the string to match against the start of the words
     * @return a list of suggested words
     */
    List<String> getSuggestionsStartingWith(String match);


    /**
     * Should be called every time a word is used to update usage statistics
     *
     * @param string the word that was used and should be updated
     */
    void updateWordUsage(String string);

}
