package no.ntnu.stud.avikeyb.backend.dictionary;

import no.ntnu.stud.avikeyb.backend.core.BackendLogger;

import java.util.*;

/**
 * Created by Tor-Martin Holen on 21-Feb-17.
 */

public class LinearEliminationDictionaryHandler implements InMemoryDictionary {
    private List<DictionaryEntry> fullDictionary;
    private List<DictionaryEntry> fullDictionaryFrequencySorted;


    public interface DictionaryChangedListener {
        void dictionaryChanged();
    }

    private DictionaryChangedListener dictionaryChangedListener = null;

    public void setDictionaryChangedListener(DictionaryChangedListener dictionaryChangedListener) {
        this.dictionaryChangedListener = dictionaryChangedListener;
    }

    /**
     * Should store all the word histories until the phrase is sent.
     */
    private List<List<SearchEntry>> phraseHistory;
    /**
     * List of suggestions given at different word lengths.
     */
    private List<SearchEntry> wordHistory;

    /**
     * A cache that eventually stores every possible first search of both modes of {@link no.ntnu.stud.avikeyb.backend.layouts.MobileLayout}
     */
    private HashMap<String, List<DictionaryEntry>> searchCache = new HashMap<>();

    /**
     * Constructs a dictionary
     */
    public LinearEliminationDictionaryHandler() {
        phraseHistory = new ArrayList<>();
        setDictionary(new ArrayList<DictionaryEntry>());
    }

    @Override
    public void setDictionary(final List<DictionaryEntry> dictionary) {
        fullDictionary = dictionary;
        fullDictionaryFrequencySorted = new ArrayList<>();
        fullDictionaryFrequencySorted.addAll(dictionary);
        ListSorter.sortList(fullDictionaryFrequencySorted, SortingOrder.FREQUENCY_HIGH_TO_LOW);
        isWordHistoryInitialized();
        if (dictionaryChangedListener != null) {
            dictionaryChangedListener.dictionaryChanged();
        }
    }

    /**
     * Finds suggestions from the previous suggestion list. The words is added to the new
     * suggestion list if the specified index matches a letter in the list. Thus the new suggestion
     * list is much smaller than the previous one.
     */
    public void findValidSuggestions(List<String> lettersToFindAtIndex, boolean nextWordOnEmptySearch) {
        List<DictionaryEntry> reducedSuggestionList;

        String letters = stringJoin(lettersToFindAtIndex);
        if (searchCache.containsKey(letters) && !hasWordHistory()) {
            reducedSuggestionList = searchCache.get(letters);
        } else {
            reducedSuggestionList = reduceValidSuggestions(lettersToFindAtIndex, getLastSuggestions());
        }

        if (reducedSuggestionList.isEmpty() && nextWordOnEmptySearch) {
            nextWord();
        } else {
            SearchEntry entry = new SearchEntry(reducedSuggestionList, lettersToFindAtIndex);
            wordHistory.add(entry);
        }

    }

    /**
     * A search method and help method to {@link #findValidSuggestions(List, boolean)} which reduces the number of plausible suggestion, to words containing one of the letters contained in the first parameter at the index calculated.
     * Note: the return value of this method must be added to the global wordHistory variable before it can be used again, if not the search index won't update.
     *
     * @param lettersToFindAtIndex the index currently being searches, usually for every search this will increment by one.
     * @param searchList           The list being searched.
     * @return A list of plausible dictionary entries matching the new search conditions.
     */
    private List<DictionaryEntry> reduceValidSuggestions(List<String> lettersToFindAtIndex, List<DictionaryEntry> searchList) {
        List<DictionaryEntry> reducedSuggestionList = new ArrayList<>();
        int searchIndex = findSearchIndex();
        //Log.d("LinearElimination", "Original suggestions: " + searchList.size());
        for (DictionaryEntry entry : searchList) {
            if (entry.getWord().length() > searchIndex && lettersToFindAtIndex.contains(String.valueOf(entry.getWord().charAt(searchIndex)))) {
                reducedSuggestionList.add(entry);
            }
        }
        return reducedSuggestionList;
    }


    /**
     * Checks if the word history has been initialized
     */
    private void isWordHistoryInitialized() {
        if (wordHistory == null) {
            wordHistory = new ArrayList<>();
            SearchEntry entry = new SearchEntry(fullDictionary, new ArrayList<String>(0));
            wordHistory.add(entry);
        }
    }

    /**
     * Removes all elements of the word history, EXCEPT the first element which is the full dictionary
     */
    public void clearWordHistory() {
        wordHistory = new ArrayList<>();
        SearchEntry entry = new SearchEntry(fullDictionary, new ArrayList<String>(0));
        wordHistory.add(entry);
    }


    /**
     * Adds word to phrase history and resets the word history to a empty list.
     * Should be called when the user selects word from dictionary
     */
    public void nextWord() {
        if (wordHistory.size() > 1) {
            phraseHistory.add(new ArrayList<>(wordHistory.subList(1, wordHistory.size())));
        } else {
            phraseHistory.add(new ArrayList<SearchEntry>());
        }

        wordHistory = new ArrayList<>(wordHistory.subList(0, 1));
    }

    /**
     * Adds the previous word to word history again and removes it from the phrase history.
     * Should be called when the user reverts to a state with no letters left in current word history.
     */
    public void previousWord() {
        int phraseHistoryIndex = phraseHistory.size() - 1;
        if (phraseHistoryIndex != -1) {
            wordHistory.clear();
            wordHistory.add(new SearchEntry(fullDictionary, new ArrayList<String>()));
            wordHistory.addAll(phraseHistory.get(phraseHistoryIndex));
            phraseHistory.remove(phraseHistoryIndex);
        }
    }

    /**
     * Prints n suggestions from the suggestion list
     *
     * @param n the amount of suggestions needed
     */
    public void printListSuggestions(int n) {
        printDictionary(getSuggestionsWithFrequencies(n));
    }

    /**
     * Reverts the suggestion history n steps, so it deletes n entries from the end of the
     * suggestion history (used to implement backspace functionality).
     *
     * @param steps number of steps to revert.
     * @return true if reversion succeeded
     */
    public boolean revertLastSearch(int steps) {
        int index = wordHistory.size() - steps;
        if (index > 0) {
            wordHistory = wordHistory.subList(0, index);
            return true;
        } else {
            return false;
        }
    }


    /**
     * Finds the index where last suggestion list should be searched to eliminate unfit words.
     *
     * @return index where findValidSuggestion() should search last suggestion list.
     */
    private int findSearchIndex() {
        if (wordHistory.size() == 0) {
            return 0;
        } else {
            return wordHistory.size() - 1;
        }

    }

    /**
     * Prints a list with the word and frequency
     *
     * @param list
     */
    public void printDictionary(List<DictionaryEntry> list) {
        for (DictionaryEntry entry : list) {
            BackendLogger.log(entry.getWord() + " - " + entry.getUserFrequency());
        }
    }

    /**
     * Adds punctuation symbols as a separate word history element for the phrase history.
     *
     * @param specialCharacter The following characters ".,!?"  are considered special.
     */
    public void addSpecialCharacterHistoryEntry(String specialCharacter) {
        clearWordHistory();
        wordHistory.add(new SearchEntry(specialCharacter));
        nextWord();
    }

    /**
     * Resets the entire phrase history and removes all but the first element of the word history
     */
    public void reset() {
        phraseHistory.clear();
        wordHistory = wordHistory.subList(0, 1);
    }

    /**
     * Checks if the current
     *
     * @return true if a element
     */
    public boolean hasWordHistory() {
        return wordHistory.size() > 1;
    }


    /**
     * Removes last element from word history.
     */
    public void removeLastWordHistoryElement() {
        wordHistory.remove(wordHistory.size() - 1);
    }


    /**
     * Checks if the current word history special, i.e it contains a punctuation symbol (.,!?)
     *
     * @return true if the word history contains a special character.
     */
    public boolean isCurrentHistoryEntrySpecial() {
        if (wordHistory.size() == 2) {
            return wordHistory.get(1).isSpecial();
        }
        return false;
    }

    /**
     * Method for caching every possible first search.
     *
     * @deprecated Not recommended due to long processing time.
     */
    public void startCaching() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                cacheLetterTileSuggestions();
                cacheLetterSuggestions();
            }
        }).start();
    }

    /**
     * Caches a tile of letters, should be called some time before the search will probably occur.
     *
     * @param letters A tile of letters from {@link no.ntnu.stud.avikeyb.backend.layouts.MobileLayout}
     */
    public void cacheInputTile(final List<String> letters) {
        String lettersJoined = stringJoin(letters);
        if (!searchCache.containsKey(lettersJoined)) {
            List<DictionaryEntry> newCacheResult = reduceValidSuggestions(letters, fullDictionaryFrequencySorted);
            searchCache.put(lettersJoined, newCacheResult);
        }
    }

    /**
     * Caches single letter searches, this should be called some time before a new unique single letter search occurs.
     *
     * @param letterList List containing single letters, each matching [a-z].
     */
    public void cacheInputSingleLetters(final List<String> letterList) {
        String letters = stringJoin(letterList);
        List<List<DictionaryEntry>> cacheSuggestions = calculateLetterCacheElements(letters);

        for (int i = 0; i < letters.length(); i++) {
            List<DictionaryEntry> letterSuggestionCache = cacheSuggestions.get(i);
            String letter = String.valueOf(letters.charAt(i));
            searchCache.put(letter, letterSuggestionCache);
        }
    }

    /**
     * Caches all letter tiles and adds them to searchCache
     *
     * @deprecated Takes to much time usable on start
     */
    private void cacheLetterTileSuggestions() {
        //Tile selection
        List<List<String>> letterGroups = new ArrayList<>();
        letterGroups.add(Arrays.asList("e", "t", "a"));
        letterGroups.add(Arrays.asList("o", "i", "n"));
        letterGroups.add(Arrays.asList("s", "r", "h"));
        letterGroups.add(Arrays.asList("l", "d", "c", "u"));
        letterGroups.add(Arrays.asList("w", "y", "b", "v"));
        letterGroups.add(Arrays.asList("m", "f", "p", "g"));
        letterGroups.add(Arrays.asList("k", "x", "j", "q", "z"));

        for (List<String> letters : letterGroups) {
            String lettersJoined = stringJoin(letters);
            List<DictionaryEntry> searchResult = reduceValidSuggestions(letters, fullDictionaryFrequencySorted);
            searchCache.put(lettersJoined, searchResult);
        }
    }

    /**
     * Caches every first search to the search cache.
     *
     * @deprecated It takes to much time to cache everything at once and this has trouble with sorting the letterTiles,
     * although the code have potential if the sorting is fixed and proper setup for waiting for it to finish is created
     */
    private void cacheLetterSuggestions() {

        String letters = "etaoinsrhldcumfpgwybvkxjqz";
        List<String> letterTiles = Arrays.asList("eta", "oin", "srh", "ldcu", "mfpg", "wybv", "kxjqz");

        List<List<DictionaryEntry>> cacheSuggestions = calculateLetterCacheElements(letters);

        for (int i = 0; i < letters.length(); i++) {
            List<DictionaryEntry> letterSuggestionCache = cacheSuggestions.get(i);
            String letter = String.valueOf(letters.charAt(i));
            searchCache.put(letter, letterSuggestionCache);
        }

        for (String letterTile : letterTiles) {
            int start = letters.indexOf(String.valueOf(letterTile.charAt(0)));
            int end = letters.indexOf(String.valueOf(letterTile.charAt(letterTile.length() - 1)));

            List<DictionaryEntry> letterSuggestionCache = new ArrayList<>();

            for (int i = start; i < end; i++) {
                letterSuggestionCache.addAll(cacheSuggestions.get(i));
            }
            ListSorter.sortList(letterSuggestionCache, SortingOrder.FREQUENCY_HIGH_TO_LOW);
            searchCache.put(letterTile, letterSuggestionCache);

        }
    }

    /**
     * Searches the dictionary list for words starting with a letter contained in the letters string.
     *
     * @param letters the letters to search for
     * @return A list containing lists with the matching dictionary entries for each letter, the list's lists corresponds to the order of the letters string.
     */
    private List<List<DictionaryEntry>> calculateLetterCacheElements(String letters) {
        List<List<DictionaryEntry>> cacheSuggestions = new ArrayList<>(letters.length());
        for (int i = 0; i < letters.length(); i++) {
            cacheSuggestions.add(new ArrayList<DictionaryEntry>());
        }

        for (DictionaryEntry entry : fullDictionaryFrequencySorted) {
            for (int i = 0; i < letters.length(); i++) {
                String letter = String.valueOf(letters.charAt(i));
                if (entry.getWord().startsWith(letter)) {
                    cacheSuggestions.get(i).add(entry);
                    break;
                }
            }
        }

        return cacheSuggestions;
    }

    /**
     * Joins a list of strings to a single string, but only the elements that consist of a single character and matchesthe regex [a-z]
     *
     * @param letters list of letters being joined
     * @return a single string of letters
     */
    private String stringJoin(List<String> letters) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < letters.size(); i++) {
            String letter = letters.get(i);
            if (letter.matches("[a-z]")) {
                result.append(letter);
            }
        }
        return result.toString();
    }

    /**
     * Help class responsible for storing searches and its corresponding result.
     * It is used in accordance with wordHistory, to provide the history feature.
     */
    private class SearchEntry {
        List<DictionaryEntry> searchResult;
        List<String> search;
        Boolean special;

        /**
         * Normal constructor for creating a search entry.
         *
         * @param searchResult The result of a search
         * @param search       A list of single letters that was searched for
         */
        public SearchEntry(List<DictionaryEntry> searchResult, List<String> search) {
            this.searchResult = searchResult;
            this.search = search;
            special = false;
        }

        /**
         * Special constructor used only when a special letter should be added to the word history.
         *
         * @param specialCharacter string containing a punctuation symbol (.,!?)
         */
        public SearchEntry(String specialCharacter) {
            DictionaryEntry dEntry = new DictionaryEntry(specialCharacter, 0, 0);
            searchResult = Collections.singletonList(dEntry);
            search = Collections.singletonList(specialCharacter);
            special = true;
        }

        public List<DictionaryEntry> getSearchResult() {
            return searchResult;
        }

        public List<String> getSearch() {
            return search;
        }

        public Boolean isSpecial() {
            return special;
        }
    }

    /**
     * @return The size of the word history list minus the default element at index zero.
     */
    public int getWordHistorySize() {
        return wordHistory.size() - 1;
    }

    /**
     * Reverts the suggestion history one step, so it deletes a entry from the end of the
     * suggestion history (used to implement backspace functionality).
     *
     * @return true if reversion succeeded
     */
    public boolean revertLastSearch() {
        return revertLastSearch(1);
    }

    /**
     * Returns the suggestion list containing n elements of the original list, sorted by frequency.
     *
     * @param n number of suggestions to include in sublist
     * @return
     */
    public List<DictionaryEntry> getSuggestionsWithFrequencies(int n) {
        List<DictionaryEntry> lastSuggestions = getLastSuggestions();
        ListSorter.sortList(lastSuggestions, SortingOrder.FREQUENCY_HIGH_TO_LOW);

        n = (lastSuggestions.size() <= n) ? lastSuggestions.size() : n;
        return new ArrayList<>(lastSuggestions.subList(0, n));
    }

    /**
     * Returns the last valid suggestions calculated in the {@link #findValidSuggestions(List, boolean)}
     *
     * @return List of dictionary entries containing valid suggestions
     */
    private List<DictionaryEntry> getLastSuggestions() {
        /*if(wordHistory.size() == 0){
            return fullDictionary;
        }else{*/
        return wordHistory.get(wordHistory.size() - 1).getSearchResult();
        //}

    }

    /**
     * @param n number of suggestions wanted
     * @return
     */
    public List<String> getDefaultSuggestion(int n) {
        n = (fullDictionaryFrequencySorted.size() <= n) ? fullDictionaryFrequencySorted.size() : n;
        List<DictionaryEntry> dictionaryEntryList = new ArrayList<>(fullDictionaryFrequencySorted.subList(0, n));
        List<String> resultList = new ArrayList<>(n);

        for (int i = 0; i < dictionaryEntryList.size(); i++) {
            DictionaryEntry de = dictionaryEntryList.get(i);
            resultList.add(de.getWord());
        }
        /*BackendLogger.log(resultList.toString());*/

        return resultList;
    }

    /**
     * Returns the suggestion list containing n elements of the original list, sorted by frequency.
     *
     * @param n number of suggestions to include in sublist
     * @return
     */
    public List<String> getSuggestions(int n) {
        synchronized (fullDictionary) {
            List<DictionaryEntry> list = getSuggestionsWithFrequencies(n);
            List<String> resultList = new ArrayList<>();
            for (DictionaryEntry entry : list) {
                resultList.add(entry.getWord());
            }
            fullDictionary.notify();
            return resultList;
        }
    }

    @Override
    public List<DictionaryEntry> getDictionary() {
        //  TODO: Is this returning the right object?
        return fullDictionary;
    }

    /**
     * Creates a list of previous searches for the current word history
     *
     * @return list of previous searches as a string.
     */
    public List<String> getHistory() {
        List<String> result = new ArrayList<>();
        for (SearchEntry entry : wordHistory) {
            List<String> search = entry.getSearch();
            String innerResult = "";
            for (int i = 0; i < search.size(); i++) {
                String subRes = search.get(i);
                /*if(i != search.size()-1){
                    innerResult += subRes + " ";
                }else {*/
                innerResult += subRes;
                //}
            }
            result.add(innerResult.toUpperCase());
        }
        result.remove(0);
        return result;
    }
}
