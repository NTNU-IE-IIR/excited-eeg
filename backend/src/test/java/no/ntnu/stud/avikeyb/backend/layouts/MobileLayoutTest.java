package no.ntnu.stud.avikeyb.backend.layouts;

import org.junit.Test;

/**
 * Created by Tor-Martin Holen on 01-Mar-17.
 */

public class MobileLayoutTest extends MobileLayoutTestBase {

    @Test
    public void writeWordTests() {
        writeTestWord();
        assertOutputBufferEquals("Test ");
    }

    @Test
    public void sendTest() {
        writeTestWord();
        selectSend();
        assertOutputBufferEquals("");
    }

    /**
     * Writes "Test, test! "
     */
    @Test
    public void writeSentences() {
        writeTestWord();
        selectComma();
        writeTestWord();
        selectExclamationMark();
        assertOutputBufferEquals("Test, test! ");
    }

    @Test
    public void deleteSingleWord() {
        writeTestWord();
        fixWord(3);
        assertOutputBufferEquals("");
        assertExpectedHistorySize(2);
        selectDictionary();
        chooseItemAt(0);
        assertOutputBufferEquals("At ");
        assertExpectedHistorySize(0);
        deleteWord(1);
        assertOutputBufferEquals("");
        assertExpectedHistorySize(0);
        writeTestWord();
        assertOutputBufferEquals("Test ");
        assertExpectedHistorySize(0);
    }

    @Test
    public void fixWordTest() {
        //Write
        writeTestWord();
        selectComma();
        writeTestWord();
        selectQuestionMark();

        //Fix
        assertOutputBufferEquals("Test, test? ");
        fixWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test, test ");
        fixWord(1);
        assertExpectedHistorySize(4);
        assertOutputBufferEquals("Test, ");
        fixWord(1);
        assertExpectedHistorySize(3);
        assertOutputBufferEquals("Test, ");
        fixWord(3);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test, ");
        fixWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test ");
        fixWord(5);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("");

    }

    @Test
    public void deleteWordTest() {
        writeTestWord();
        selectComma();
        writeTestWord();
        selectQuestionMark();
        assertOutputBufferEquals("Test, test? ");
        deleteWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test, test ");
        deleteWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test, ");
        deleteWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test ");
        deleteWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("");
    }

    @Test
    public void modeChangedEndWordWithDictionary() {
        selectToggleMode();
        selectETA_ETI_();
        chooseItemAt(1);
        selectETA_ETI_();
        chooseItemAt(0);
        selectSRH_OSD();
        chooseItemAt(1);
        selectETA_ETI_();
        chooseItemAt(1);
        assertOutputBufferEquals("Test");
        assertExpectedHistorySize(4);
        selectDictionary();
        chooseItemAt(0);
        assertOutputBufferEquals("Test ");
        assertExpectedHistorySize(0);
        fixWord(1);
        assertOutputBufferEquals("Test");
        assertExpectedHistorySize(4);
        selectDictionary();
        chooseItemAt(0);
        assertOutputBufferEquals("Test ");
        assertExpectedHistorySize(0);
    }

    @Test
    public void modeChangedChooseWordWithDictionary() {
        selectToggleMode();
        selectETA_ETI_();
        chooseItemAt(1);
        selectETA_ETI_();
        chooseItemAt(0);
        selectSRH_OSD();
        chooseItemAt(1);
        assertExpectedHistorySize(3);
        selectDictionary();
        chooseItemAt(0);
        assertOutputBufferEquals("Test ");
    }

    @Test
    public void defaultNumberOfSuggestions(){
        selectDictionary();
        assertExpectedNumberOfSuggestions(20);
    }

    @Test
    public void modeChangedEndWordWithSpace() {
        selectToggleMode();
        selectETA_ETI_();
        chooseItemAt(1);
        selectETA_ETI_();
        chooseItemAt(0);
        selectSRH_OSD();
        chooseItemAt(1);
        selectETA_ETI_();
        chooseItemAt(1);
        assertOutputBufferEquals("Test");
        assertExpectedHistorySize(4);
        selectETA_ETI_();
        chooseItemAt(3);
        assertOutputBufferEquals("Test ");
        assertExpectedHistorySize(0);
        fixWord(1);
        assertOutputBufferEquals("Test");
        assertExpectedHistorySize(4);
        fixWord(1);
        assertOutputBufferEquals("Tes");
        assertExpectedHistorySize(3);
        selectDictionary();
        chooseItemAt(0);
        assertOutputBufferEquals("Test ");
    }

    @Test
    public void modeChangedDeleteWord() {
        testSentenceModeChanged();

        deleteWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test, test ");

        deleteWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test, ");

        deleteWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test ");

        deleteWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("");
    }

    @Test
    public void modeChangedFixWord() {
        testSentenceModeChanged();
        fixWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test, test ");

        fixWord(1);
        assertExpectedHistorySize(4);
        assertOutputBufferEquals("Test, test");

        fixWord(1);
        assertExpectedHistorySize(3);
        assertOutputBufferEquals("Test, tes");

        fixWord(3);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test, ");

        fixWord(1);
        assertExpectedHistorySize(0);
        assertOutputBufferEquals("Test ");

        fixWord(1);
        assertExpectedHistorySize(4);
        assertOutputBufferEquals("Test");

    }

    @Test
    public void modeChangedSelectDictionaryDefaultWord() {
        selectToggleMode();
        selectDictionary();
        chooseItemAt(0);
        assertOutputBufferEquals("Hello ");
    }

    @Test
    public void SelectDictionaryDefaultWord() {
        selectDictionary();
        chooseItemAt(0);
        assertOutputBufferEquals("Hello ");
    }

    /**
     * Writes "Test "
     */
    private void writeTestWord() {
        selectToggleMode();
        selectToggleMode();
        //TODO figure out why tests break when run without toggling mode twice

        selectETA_ETI_();
        assertExpectedHistorySize(1);
        selectETA_ETI_();
        assertExpectedHistorySize(2);
        selectSRH_OSD();
        assertExpectedHistorySize(3);
        selectETA_ETI_();
        assertExpectedHistorySize(4);
        selectDictionary();
        chooseItemAt(0);
    }

    private void testSentenceModeChanged() {
        selectToggleMode();
        writeTestWordModeChanged();
        selectComma();
        writeTestWordModeChanged();
        selectQuestionMark();
        assertOutputBufferEquals("Test, test? ");
        assertExpectedHistorySize(0);
    }

    private void writeTestWordModeChanged() {
        selectETA_ETI_();
        chooseItemAt(1);
        selectETA_ETI_();
        chooseItemAt(0);
        selectSRH_OSD();
        chooseItemAt(1);
        selectETA_ETI_();
        chooseItemAt(1);
        selectETA_ETI_();
        chooseItemAt(3);
    }

}
