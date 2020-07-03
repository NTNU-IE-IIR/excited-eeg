package no.ntnu.stud.avikeyb.backend.core;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.dictionary.Dictionary;

import static org.junit.Assert.assertEquals;

/**
 * Created by pitmairen on 27/02/2017.
 */
public class WordUpdaterTest {

    private LoggingDictionary dictionary;
    private Keyboard keyboard;

    @Before
    public void setUp() throws Exception {

        dictionary = new LoggingDictionary();
        keyboard = new CoreKeyboard();
        WordUpdater wordUpdater = new WordUpdater(dictionary);
        keyboard.addOutputDevice(wordUpdater);
    }

    @Test
    public void testUpdateWordCount() throws Exception {
        keyboard.addToCurrentBuffer("hello this is a test");

        keyboard.sendCurrentBuffer();

        assertUpdateCount("hello", 1);
        assertUpdateCount("this", 1);
        assertUpdateCount("is", 1);
        assertUpdateCount("a", 1);
        assertUpdateCount("test", 1);

    }


    @Test
    public void testUpdateWordOnlyOnceInTheSameSendOperation() throws Exception {
        keyboard.addToCurrentBuffer("hello test test this hello");

        keyboard.sendCurrentBuffer();

        assertUpdateCount("hello", 1);
        assertUpdateCount("test", 1);
        assertUpdateCount("this", 1);

    }

    @Test
    public void testOnlyUpdateAfterSend() throws Exception {
        keyboard.addToCurrentBuffer("test1 test2");

        assertUpdateCount("test1", 0);
        assertUpdateCount("test2", 0);

        keyboard.addToCurrentBuffer(" test3");

        assertUpdateCount("test1", 0);
        assertUpdateCount("test2", 0);
        assertUpdateCount("test3", 0);


        keyboard.sendCurrentBuffer();


        assertUpdateCount("test1", 1);
        assertUpdateCount("test2", 1);
        assertUpdateCount("test3", 1);

    }


    @Test
    public void testPartialWords() throws Exception {
        keyboard.addToCurrentBuffer("test");

        assertUpdateCount("test", 0);

        keyboard.addToCurrentBuffer("ing");

        assertUpdateCount("test", 0);
        assertUpdateCount("testing", 0);


        keyboard.sendCurrentBuffer();

        assertUpdateCount("test", 0);
        assertUpdateCount("testing", 1);

    }


    @Test
    public void testMultipleSend() throws Exception {
        keyboard.addToCurrentBuffer("test1 test2");

        assertUpdateCount("test1", 0);
        assertUpdateCount("test2", 0);

        keyboard.sendCurrentBuffer();

        assertUpdateCount("test1", 1);
        assertUpdateCount("test2", 1);

        keyboard.addToCurrentBuffer("test3");

        keyboard.sendCurrentBuffer(); // only test3 should be in this send

        assertUpdateCount("test1", 1);
        assertUpdateCount("test2", 1);
        assertUpdateCount("test3", 1);


        keyboard.addToCurrentBuffer("test1 test2");


        keyboard.sendCurrentBuffer(); // only test1 and test2 should update


        assertUpdateCount("test1", 2);
        assertUpdateCount("test2", 2);
        assertUpdateCount("test3", 1);

    }


    private void assertUpdateCount(String word, int expectedCount) {
        assertEquals(expectedCount, dictionary.getUpdateCount(word));
    }


    /**
     * Test that special symbols are removed before updating the word count
     */
    @Test
    public void testOnlyLogWordCharacters() throws Exception {

        List<String> words = new ArrayList<>();
        String[] specialChars = {",", ".", "!", "?"};

        int i = 0;
        for (String specialChar : specialChars) {
            words.add("test" + i); // the word that should be stored
            keyboard.addToCurrentBuffer("te" + specialChar + "st" + i + specialChar + " "); // the same word with some non word characters added
            i++;
        }
        keyboard.sendCurrentBuffer();

        for (String word : words) {
            assertUpdateCount(word, 1);
        }
    }


    /**
     * Used for logging the word update count
     */
    private static class LoggingDictionary implements Dictionary {

        private Map<String, Integer> updateCount = new HashMap<>();


        public int getUpdateCount(String word) {
            if (!updateCount.containsKey(word)) {
                return 0;
            }
            return updateCount.get(word);
        }

        @Override
        public List<String> getSuggestionsStartingWith(String match) {
            return Collections.emptyList();
        }

        @Override
        public void updateWordUsage(String string) {
            if (!updateCount.containsKey(string)) {
                updateCount.put(string, 0);
            }
            updateCount.put(string, updateCount.get(string) + 1);
        }
    }

}