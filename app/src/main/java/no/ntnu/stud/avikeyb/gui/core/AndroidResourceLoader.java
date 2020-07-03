package no.ntnu.stud.avikeyb.gui.core;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import no.ntnu.stud.avikeyb.backend.dictionary.DictionaryEntry;
import no.ntnu.stud.avikeyb.backend.dictionary.ResourceHandler;

/**
 * Android specific resource loader.
 *
 * @author ?
 * @author Kristian Honningsvag.
 */
public class AndroidResourceLoader {

    /**
     * Loads a dictionary from the provided file resource id.
     *
     * @param context    a context object
     * @param resourceId the resource id of the file to load
     * @return a list of dictionary entries
     */
    public static List<DictionaryEntry> loadDictionaryFromResource(Context context, int resourceId) {
        List<DictionaryEntry> dictionary = new ArrayList<>();
        try {
            dictionary = ResourceHandler.loadDictionaryFromStream(context.getResources().openRawResource(resourceId));
        } catch (Exception e) {
            System.err.println("Failed loading dictionary from resourceID " + resourceId);
            e.printStackTrace();
        }
        return dictionary;
    }

}
