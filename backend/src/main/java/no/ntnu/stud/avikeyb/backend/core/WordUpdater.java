package no.ntnu.stud.avikeyb.backend.core;

import java.util.HashSet;
import java.util.Set;

import no.ntnu.stud.avikeyb.backend.OutputDevice;
import no.ntnu.stud.avikeyb.backend.dictionary.Dictionary;

/**
 * Output device for the keyboard that will log all words sent by the keyboard and pass them on to
 * the dictionaryHandler to update the usage count for the typed words.
 */
public class WordUpdater implements OutputDevice {

    private Dictionary dictionaryHandler;

    /**
     * Create a new word updater
     *
     * @param dictionaryHandler the dictionaryHandler instance that should be updated with word usage count
     */
    public WordUpdater(Dictionary dictionaryHandler) {
        this.dictionaryHandler = dictionaryHandler;
    }

    @Override
    public void sendOutput(String output) {
        Set<String> words = getWordsInBuffer(output);
        for (String word : words) {
            dictionaryHandler.updateWordUsage(word);
        }
    }

    // Extracts all words from the string. Special characters will be removed from the words.
    private Set<String> getWordsInBuffer(String buffer) {

        Set<String> words = new HashSet<>();
        for (String word : buffer.split(" ")) {
            word = stripWord(word);
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;

    }

    // Remove special characters from the word
    private String stripWord(String word) {
        return word.replaceAll("[^\\w]", "");
    }


}
