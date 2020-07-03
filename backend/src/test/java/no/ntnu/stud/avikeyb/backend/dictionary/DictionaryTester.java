package no.ntnu.stud.avikeyb.backend.dictionary;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Definition of default tests for the implementations of the dictionary interface
 */
public abstract class DictionaryTester {

    private Dictionary defaultDictionary;


    protected abstract Dictionary createDictionary(List<DictionaryEntry> entries);


    @Before
    public void setUp() throws Exception {

        defaultDictionary = createDictionary(Arrays.asList(
                de("test11", 8),
                de("test2", 7),
                de("test3", 6),
                de("hello1", 5),
                de("hello2", 4),
                de("hello3", 3),
                de("testing4", 2),
                de("testing55", 1)
        ));
    }

    @Test
    public void testEmptyDictionary() throws Exception {
        Dictionary dictionary = createDictionary(Collections.<DictionaryEntry>emptyList());
        List<String> res = dictionary.getSuggestionsStartingWith("test");
        assertThat(res, is(Collections.<String>emptyList()));
    }


    @Test
    public void testSingleElementNoMatch() throws Exception {
        Dictionary dictionary = createDictionary(Arrays.asList(de("test", 0)));
        List<String> res = dictionary.getSuggestionsStartingWith("hello");
        assertThat(res, is(Collections.<String>emptyList()));
    }

    /**
     * Word longer than the word in defaultDictionary
     */
    @Test
    public void testSingleElementNoMatchTooLong() throws Exception {
        Dictionary dictionary = createDictionary(Arrays.asList(de("test", 0)));
        List<String> res = dictionary.getSuggestionsStartingWith("testing");
        assertThat(res, is(Collections.<String>emptyList()));
    }

    @Test
    public void testSingleElementWithMatch() throws Exception {
        Dictionary dictionary = createDictionary(Arrays.asList(de("test", 0)));
        List<String> res = dictionary.getSuggestionsStartingWith("te");
        assertThat(res, is(Arrays.asList("test")));
    }

    @Test
    public void testDontIncludePerfectMatch() throws Exception {
        Dictionary dictionary = createDictionary(Arrays.asList(de("test", 0)));
        // perfect match
        List<String> res = dictionary.getSuggestionsStartingWith("test");
        assertThat(res, is(Collections.<String>emptyList()));
    }

    @Test
    public void testMultipleWords() throws Exception {

        List<String> res = defaultDictionary.getSuggestionsStartingWith("te");
        assertThat(res, is(Arrays.asList("test11", "test2", "test3", "testing4", "testing55")));

        res = defaultDictionary.getSuggestionsStartingWith("testing");
        assertThat(res, is(Arrays.asList("testing4", "testing55")));


        res = defaultDictionary.getSuggestionsStartingWith("testing6");
        assertThat(res, is(Collections.<String>emptyList()));


        // Don't include perfect match
        res = defaultDictionary.getSuggestionsStartingWith("test2");
        assertThat(res, is(Collections.<String>emptyList()));

        res = defaultDictionary.getSuggestionsStartingWith("hel");
        assertThat(res, is(Arrays.asList("hello1", "hello2", "hello3")));
    }


    @Test
    public void testMatchLastWord() throws Exception {
        // Test the word that should be the last word in the sorted dictionary
        List<String> res = defaultDictionary.getSuggestionsStartingWith("testing5");
        assertThat(res, is(Arrays.asList("testing55")));
    }


    @Test
    public void testMatchFirstWord() throws Exception {
        // Test the word that should be the last word in the sorted dictionary
        List<String> res = defaultDictionary.getSuggestionsStartingWith("test1");
        assertThat(res, is(Arrays.asList("test11")));
    }

    @Test
    public void testStandardFrequencies() throws Exception {
        // User frequencies are all 0
        Dictionary dictionary = createDictionary(Arrays.asList(
                de("test1", 1),
                de("test2", 10),
                de("test3", 4),
                de("testing4", 2),
                de("testing5", 2),
                de("testing6", 8),
                de("testing7", 4)
        ));

        List<String> res = dictionary.getSuggestionsStartingWith("test");
        assertThat(res, is(Arrays.asList("test2", "testing6", "test3", "testing7", "testing4", "testing5", "test1")));
    }


    @Test
    public void testUserFrequencies() throws Exception {
        // User frequency should count more than the standard frequency, but the standard frequency
        // should still be used for sorting as well
        Dictionary dictionary = createDictionary(Arrays.asList(
                de("test1", 1, 100),
                de("test2", 10, 0),
                de("test3", 4, 50),
                de("testing4", 2),
                de("testing5", 2),
                de("testing6", 8),
                de("testing7", 4)
        ));

        List<String> res = dictionary.getSuggestionsStartingWith("test");
        assertThat(res, is(Arrays.asList("test1", "test3", "test2", "testing6", "testing7", "testing4", "testing5")));
    }


    @Test
    public void testUpdateWordUsage() throws Exception {

        Dictionary dictionary = createDictionary(Arrays.asList(
                de("test1", 1),
                de("test2", 10),
                de("test3", 4),
                de("testing4", 2),
                de("testing5", 2),
                de("testing6", 8),
                de("testing7", 4)
        ));

        List<String> res = dictionary.getSuggestionsStartingWith("test");

        // Should be sorted on standard frequency
        assertThat(res, is(Arrays.asList("test2", "testing6", "test3", "testing7", "testing4", "testing5", "test1")));

        dictionary.updateWordUsage("test1");
        res = dictionary.getSuggestionsStartingWith("test");

        // Should be sorted on standard frequency, with test1 first because of 1 usage count
        assertThat(res, is(Arrays.asList("test1", "test2", "testing6", "test3", "testing7", "testing4", "testing5")));

        dictionary.updateWordUsage("testing5");
        res = dictionary.getSuggestionsStartingWith("test");

        // testing5 should be before test1 because of higher standard frequency
        assertThat(res, is(Arrays.asList("testing5", "test1", "test2", "testing6", "test3", "testing7", "testing4")));

        dictionary.updateWordUsage("testing7");
        dictionary.updateWordUsage("testing7");
        res = dictionary.getSuggestionsStartingWith("test");

        // testing7 should the first because of highest user frequency
        assertThat(res, is(Arrays.asList("testing7", "testing5", "test1", "test2", "testing6", "test3", "testing4")));

        dictionary.updateWordUsage("testing5");
        res = dictionary.getSuggestionsStartingWith("test");

        // testing7 and testing5 has equal user freqency, but testing7 has higher standard frequency
        assertThat(res, is(Arrays.asList("testing7", "testing5", "test1", "test2", "testing6", "test3", "testing4")));

        dictionary.updateWordUsage("testing5");
        res = dictionary.getSuggestionsStartingWith("test");

        // testing5 has highest user frequency
        assertThat(res, is(Arrays.asList("testing5", "testing7", "test1", "test2", "testing6", "test3", "testing4")));


    }


    @Test
    public void testSearchEmptyString() throws Exception {
        // User frequency should count more than the standard frequency, but the standard frequency
        // should still be used for sorting as well
        Dictionary dictionary = createDictionary(Arrays.asList(
                de("test1", 1, 100),
                de("test2", 10, 0),
                de("test3", 4, 50),
                de("testing4", 2),
                de("testing5", 2),
                de("testing6", 8),
                de("testing7", 4),
                de("hello", 1, 40),
                de("cake", 14, 3)

        ));

        List<String> res = dictionary.getSuggestionsStartingWith("");
        assertThat(res, is(Arrays.asList("test1", "test3", "hello", "cake", "test2", "testing6", "testing7", "testing4", "testing5")));
    }


    @Test
    public void testNewWords() throws Exception {

        Dictionary dictionary = createDictionary(new ArrayList<DictionaryEntry>(Arrays.asList(
                de("test1", 1),
                de("test2", 10),
                de("test3", 4),
                de("testing4", 2),
                de("testing5", 2),
                de("testing6", 8),
                de("testing7", 4)
        )));

        dictionary.updateWordUsage("tester");
        List<String> res = dictionary.getSuggestionsStartingWith("test");

        assertThat(res, is(Arrays.asList("tester", "test2", "testing6", "test3", "testing7", "testing4", "testing5", "test1")));


        dictionary.updateWordUsage("test3");
        res = dictionary.getSuggestionsStartingWith("test");

        assertThat(res, is(Arrays.asList("test3", "tester", "test2", "testing6", "testing7", "testing4", "testing5", "test1")));

    }


    private DictionaryEntry de(String word, int frequency) {
        return new DictionaryEntry(word, frequency, 0);
    }

    private DictionaryEntry de(String word, int frequency, int userFreq) {
        return new DictionaryEntry(word, frequency, userFreq);
    }


}
