package no.ntnu.stud.avikeyb.backend.dictionary;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static junit.framework.Assert.assertEquals;


/**
 * Unit tests for the resource handler.
 * <p>
 * Created by Kristian Honningsvag.
 */

public class ResourceHandlerTest {

    private InMemoryDictionary dictionaryHandler;

    /**
     * Working directory for tests are: /AccessibleVirtualKeyboard/AViKEYB/app
     */
    @Before
    public void setUp() {
        dictionaryHandler = new DictionaryHandler(new DictionaryFileLoader(getClass().getClassLoader().getResource("test_dictionary.txt").getPath()).loadDictionary());
    }

    /**
     * Test storing a dictionary that has been loaded into memory.
     */
    @Test
    public void testStoreDictionaryEntries() {


        boolean isStored = false;
        Path tmpFile = null;
        try {
            tmpFile = Files.createTempFile("dict_test", "dict");
            ResourceHandler.storeDictionaryToFile(dictionaryHandler.getDictionary(), tmpFile.toString());
            isStored = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(tmpFile != null) {
                try {
                    Files.deleteIfExists(tmpFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to delete tmp dictionary file");
                }
            }
        }
        assertEquals(true, isStored);
    }

}
