package no.ntnu.stud.avikeyb.backend.dictionary;

/**
 * Represents an entry in the dictionary.
 * Help class used for DictionaryHandler class.
 *
 * @author Tor-Martin Holen on 01-Feb-17 (Originally 21-Feb-17).
 * @author Kristian Honningsvag.
 */
public class DictionaryEntry {
    private String word;
    private int standardFrequency;
    private int userFrequency;

    @Deprecated
    public DictionaryEntry(String word, int frequency) {
        this.word = word;
        this.userFrequency = frequency;
    }

    /**
     * Constructs the dictionary entry.
     *
     * @param word              The word.
     * @param standardFrequency The standard usage frequency of the word. This should be based on
     *                          general statistically average and should rarely change.
     * @param userFrequency     The frequency of use for a particular user. Should get updated each
     *                          time this word is typed.
     */
    public DictionaryEntry(String word, int standardFrequency, int userFrequency) {
        this.word = word;
        this.standardFrequency = standardFrequency;
        this.userFrequency = userFrequency;
    }

    /**
     * Returns the entry as a string.
     */
    public String getEntryAsString() {
        return "DictionaryEntry{word='" + word + "', standardFrequency=" + standardFrequency + "', userFrequency=" + userFrequency + "}";
    }

    // Getters.
    public String getWord() {
        return word;
    }

    public int getStandardFrequency() {
        return standardFrequency;
    }

    public int getUserFrequency() {
        return userFrequency;
    }


    // Setters.
    public void setStandardFrequency(int standardFrequency) {
        this.standardFrequency = standardFrequency;
    }

    public void setUserFrequency(int userFrequency) {
        this.userFrequency = userFrequency;
    }

    @Override
    public String toString() {
        return "DictionaryEntry{" +
                "word='" + word + '\'' +
                '}';
    }
}