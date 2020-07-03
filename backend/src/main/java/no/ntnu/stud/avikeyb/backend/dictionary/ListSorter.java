package no.ntnu.stud.avikeyb.backend.dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Tor-Martin Holen on 24-Feb-17.
 */

public abstract class ListSorter {
    /**
     * Sorts list according to an order from the SortingOrder enum
     *
     * @param list  List to sort
     * @param order SortingOrder Enum
     */
    static void sortList(List<DictionaryEntry> list, final SortingOrder order){
        List<Comparator<DictionaryEntry>> comparator = new ArrayList<>();

        if (order != SortingOrder.CURRENT_ORDER) {
            if (order == SortingOrder.ALPHABETICALLY_A_TO_Z) {
                comparator.add(new Comparator<DictionaryEntry>() {
                    @Override
                    public int compare(DictionaryEntry o1, DictionaryEntry o2) {
                        return o1.getWord().compareTo(o2.getWord());
                    }
                });
            }
            if (order == SortingOrder.ALPHABETICALLY_Z_TO_A) {
                comparator.add(new Comparator<DictionaryEntry>() {
                    @Override
                    public int compare(DictionaryEntry o1, DictionaryEntry o2) {
                        return o2.getWord().compareTo(o1.getWord());
                    }
                });
            }
            if (order == SortingOrder.FREQUENCY_HIGH_TO_LOW) {
                comparator.add(new Comparator<DictionaryEntry>() {
                    @Override
                    public int compare(DictionaryEntry o1, DictionaryEntry o2) {
                        int cmp = Integer.compare(o2.getUserFrequency(), o1.getUserFrequency());
                        if(cmp == 0) {
                            cmp = Integer.compare(o2.getStandardFrequency(), o1.getStandardFrequency());
                        }
                        return cmp;
                    }
                });

                        /*ComparisonChain.start()
                        .compare(o2.getUserFrequency(),o1.getUserFrequency())
                        .compare(o2.getStandardFrequency(),o1.getStandardFrequency())
                        .compare(o1.getWord(),o2.getWord()).result();*/
            }
            if (order == SortingOrder.FREQUENCY_LOW_TO_HIGH) {
                comparator.add(new Comparator<DictionaryEntry>() {
                    @Override
                    public int compare(DictionaryEntry o1, DictionaryEntry o2) {
                        int cmp = Integer.compare(o1.getUserFrequency(), o2.getUserFrequency());
                        if (cmp == 0) {
                            cmp = Integer.compare(o1.getStandardFrequency(), o2.getStandardFrequency());
                        }
                        return cmp;
                    }
                });
                        /*ComparisonChain.start()
                        .compare(o1.getUserFrequency(),o2.getUserFrequency())
                        .compare(o1.getStandardFrequency(),o2.getStandardFrequency())
                        .compare(o1.getWord(),o2.getWord()).result();*/
            }
            for (Comparator<DictionaryEntry> comp:comparator) {
                Collections.sort(list, comp);
            }

        }
    }
}
