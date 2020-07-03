package no.ntnu.stud.avikeyb.backend.dictionary;

import java.util.*;

/**
 * Handles dictionary functions.
 *
 * @author Kristian Honningsvag.
 */
public class DictionaryHandler implements Dictionary, InMemoryDictionary {

    private List<DictionaryEntry> dictionary;
    private PriorityQueue<DictionaryEntry> mostUsedWords;
    private Map<String, DictionaryEntry> dictionaryEntryLookup; // used to speed up frequency updates
    private final int mostUsedWordsLimit;

    /**
     * Constructs a dictionary handler with an empty dictionary.
     */
    public DictionaryHandler() {
        this(new ArrayList<DictionaryEntry>());
    }

    /**
     * Constructs the dictionary handler.
     * Automatically sorts the dictionary alphabetically.
     *
     * @param dictionary
     */
    public DictionaryHandler(List<DictionaryEntry> dictionary) {
        this(dictionary, 20);
    }

    /**
     * Constructs the dictionary handler.
     * Automatically sorts the dictionary alphabetically.
     *
     * @param dictionary
     */
    public DictionaryHandler(List<DictionaryEntry> dictionary, int mostUsedWordsLimit) {
        this.dictionary = dictionary;
        this.mostUsedWords = new PriorityQueue<>(mostUsedWordsLimit + 1, dictionaryEntryComparatorLeastUsedFirst());
        this.dictionaryEntryLookup = new HashMap<>();
        this.mostUsedWordsLimit = mostUsedWordsLimit;
        updateMostUsedWords(dictionary);
        updateEntryLookup(dictionary);
        sortDictionary();
    }

    /**
     * Sorts the dictionary in an alphabetically order.
     */
    public void sortDictionary() {
        Collections.sort(dictionary, new Comparator<DictionaryEntry>() {
            @Override
            public int compare(DictionaryEntry o1, DictionaryEntry o2) {
                return o1.getWord().compareToIgnoreCase(o2.getWord());
            }
        });
    }

    /**
     * Performs a prefix search on the dictionary, returning a list of all words that begin with the
     * prefix. The returned list is sorted by the words frequency of occurrence.
     * <p>
     * This method expects the dictionary to be in an alphabetical ordering.
     *
     * @param prefix The prefix.
     * @return All words that begin with the prefix.
     */
    private List<String> prefixSearchBinary(String prefix) {

        // Return empty list if invalid input, or if dictionary is empty.
        if (prefix == null || dictionary.isEmpty()) {
            return new ArrayList<String>();
        }

        // Return the most used words sorted by use frequency when the search string is empty
        if(prefix.isEmpty()){
            return sortAndExtractSuggestions(Arrays.asList(mostUsedWords.toArray(new DictionaryEntry[]{})));
        }


        ArrayList<String> matchingWords = new ArrayList<>();
        ArrayList<DictionaryEntry> matchingDictionaryEntries = new ArrayList<>();
        String currentWord = null;
        boolean doneSearching = false;
        boolean allFound = false;

        int leftLimit = 0;
        int rightLimit = dictionary.size() - 1;
        int searchLocation = (leftLimit + rightLimit) / 2;

        // Find the word with the lowest string value that starts with the prefix.
        while (!doneSearching) {

            // Get dictionary word at current location.
            currentWord = dictionary.get(searchLocation).getWord();
            int delta = prefix.compareToIgnoreCase(currentWord);

            // When there are only two alternatives left.
            if ((rightLimit - leftLimit) <= 1) {
                if (delta > 0) {
                    searchLocation = rightLimit;
                } else if (delta < 0) {
                    searchLocation = leftLimit;
                } else if (delta == 0) {
                    // Do nothing. We are in the correct position.
                }
                currentWord = dictionary.get(searchLocation).getWord();
                doneSearching = true;
            }

            if (!doneSearching) {
                // Find out if we need to search backwards or forwards.
                if (delta < 0) {
                    // Target might be behind us.
                    rightLimit = searchLocation;
                    searchLocation = (leftLimit + rightLimit) / 2;
                } else if (delta > 0) {
                    // Target might be ahead of us.
                    leftLimit = searchLocation;
                    searchLocation = (leftLimit + rightLimit) / 2;
                } else if (delta == 0) {
                    // Landed directly on the first matching word.
                    doneSearching = true;
                }
//            System.out.println("L:" + leftLimit + " M:" + searchLocation + " R:" + rightLimit);
            }
        }

        // Iterate forwards and collect all the matches.
        while (!allFound) {
            if (currentWord.startsWith(prefix)) {
                if (!currentWord.equalsIgnoreCase(prefix)) {  // Don't add the word if it is identical to the prefix.
                    matchingDictionaryEntries.add(dictionary.get(searchLocation));
                }
                searchLocation += 1;
                if (searchLocation < dictionary.size()) {
                    currentWord = dictionary.get(searchLocation).getWord();
                } else {
                    // Reached end of dictionary.
                    allFound = true;
                }
            } else {
                // There are now more matches.
                allFound = true;
            }
        }

        return sortAndExtractSuggestions(matchingDictionaryEntries);
    }

    /**
     * Adds a new word to the dictionary.
     *
     * @param word              Word to be added.
     * @param standardFrequency The standard usage frequency.
     * @param userFrequency     The user specific usage frequency.
     * @return 1 if word was successfully added. -1 if there was an error.
     */
    public int addWordToDictionary(String word, int standardFrequency, int userFrequency) {

        // First check if word already exists.
        if(dictionaryEntryLookup.containsKey(word.toLowerCase())){
            return -1;
        }

        DictionaryEntry entry = new DictionaryEntry(word, standardFrequency, userFrequency);
        dictionary.add(entry);
        updateMostUsedWords(entry);
        updateEntryLookup(entry);
        sortDictionary();
        return 1;
    }

    /**
     * Prints the entire dictionary.
     */
    public void printDictionary() {
        for (DictionaryEntry entry : dictionary) {
            System.out.println(entry.getEntryAsString());
        }
    }

    /**
     * Takes all duplicate entries in this dictionary and merges them into one.
     */
    public void mergeDuplicateEntries() {
        DictionaryEntry previousEntry = null;
        for (int i = 0; i < dictionary.size(); i++) {
            DictionaryEntry currentEntry = dictionary.get(i);
            if (previousEntry != null && previousEntry.getWord().equals(currentEntry.getWord())) {
                previousEntry.setUserFrequency(previousEntry.getUserFrequency() + currentEntry.getUserFrequency());
                previousEntry.setStandardFrequency(previousEntry.getStandardFrequency() + currentEntry.getStandardFrequency());
                dictionary.remove(currentEntry);
                i--;
            } else {
                previousEntry = currentEntry;
            }
        }
        sortDictionary();
    }

    // Overridden functions.
    @Override
    public List<String> getSuggestionsStartingWith(String match) {
        return prefixSearchBinary(match);
    }

    @Override
    public void updateWordUsage(String string) {
        string = string.toLowerCase();
        if (dictionaryEntryLookup.containsKey(string)) {
            DictionaryEntry entry = dictionaryEntryLookup.get(string);
            entry.setUserFrequency(entry.getUserFrequency() + 1);
            updateMostUsedWords(entry);
        }else{
            // Word does not exist in dictionary. Add it.
            addWordToDictionary(string, 0, 1);
        }
    }

    @Override
    public void setDictionary(List<DictionaryEntry> dictionary) {
        this.dictionary = dictionary;
        dictionaryEntryLookup.clear();
        mostUsedWords.clear();
        updateMostUsedWords(dictionary);
        updateEntryLookup(dictionary);
        sortDictionary();
    }

    @Override
    public List<DictionaryEntry> getDictionary() {
        return dictionary;
    }


    private List<String> sortAndExtractSuggestions(List<DictionaryEntry> entries){

        Collections.sort(entries, dictionaryEntryComparatorMostUsedFirst());
        List<String> suggestions = new ArrayList<>();
        // Get only the words.
        for (DictionaryEntry dictionaryEntry : entries) {
            suggestions.add(dictionaryEntry.getWord());
        }
        return suggestions;
    }

    // Compare by user frequency and standard frequency
    private Comparator<DictionaryEntry> dictionaryEntryComparatorMostUsedFirst(){
        return new Comparator<DictionaryEntry>() {
            @Override
            public int compare(DictionaryEntry d1, DictionaryEntry d2) {
                int cmp = Integer.compare(d2.getUserFrequency(), d1.getUserFrequency());
                if(cmp == 0){
                    cmp = Integer.compare(d2.getStandardFrequency(), d1.getStandardFrequency());
                    if(cmp == 0){
                        cmp = d1.getWord().compareToIgnoreCase(d2.getWord());
                    }
                }
                return cmp;
            }
        };
    }

    // Compare by user frequency and standard frequency
    private Comparator<DictionaryEntry> dictionaryEntryComparatorLeastUsedFirst(){
        return new Comparator<DictionaryEntry>() {
            @Override
            public int compare(DictionaryEntry d1, DictionaryEntry d2) {
                int cmp = Integer.compare(d1.getUserFrequency(), d2.getUserFrequency());
                if(cmp == 0){
                    cmp = Integer.compare(d1.getStandardFrequency(), d2.getStandardFrequency());
                }
                return cmp;
            }
        };
    }

    private void updateEntryLookup(List<DictionaryEntry> entries){
        for(DictionaryEntry entry : entries){
            updateEntryLookup(entry);
        }
    }

    private void updateEntryLookup(DictionaryEntry entry){
        dictionaryEntryLookup.put(entry.getWord().toLowerCase(), entry);
    }

    private void updateMostUsedWords(List<DictionaryEntry> entries){
        for(DictionaryEntry ent : entries){
            updateMostUsedWords(ent);
        }
    }


    // Update top word usage.
    private void updateMostUsedWords(DictionaryEntry entry){

        DictionaryEntry leastEntry = mostUsedWords.peek();

        // Because the standard java priority queue can not be capped to a specific size we have to do it manually
        // Keep only the 20 most used words
        if(mostUsedWords.size() >= mostUsedWordsLimit) {

            int cmp = mostUsedWords.comparator().compare(leastEntry, entry);

            if (cmp < 0) {
                // Remove the entry if it is already in the queue, else we remove the least element before adding the new
                if(!mostUsedWords.remove(entry)){
                    mostUsedWords.poll();
                }
                mostUsedWords.add(entry);
            }
        }else {
            mostUsedWords.add(entry);
        }
    }
}
