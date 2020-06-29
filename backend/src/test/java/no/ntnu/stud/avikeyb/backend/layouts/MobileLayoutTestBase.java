package no.ntnu.stud.avikeyb.backend.layouts;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Layout;
import no.ntnu.stud.avikeyb.backend.dictionary.DictionaryEntry;
import no.ntnu.stud.avikeyb.backend.dictionary.DictionaryFileLoader;
import no.ntnu.stud.avikeyb.backend.dictionary.LinearEliminationDictionaryHandler;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Tor Martin Holen on 24-Mar-17.
 */
public class MobileLayoutTestBase extends LayoutTestBase {

    protected MobileLayout layout;
    @Override
    protected Layout createLayout() {
        List<DictionaryEntry> entries = new DictionaryFileLoader(getClass().getClassLoader().getResource("dictionary.txt").getPath()).loadDictionary();
        layout = new MobileLayout(keyboard, new LinearEliminationDictionaryHandler());
        layout.setDictionaryList(entries);
        return layout;
    }
    
    protected void selectETA_ETI_(){
        select(2);
    }

    protected void selectOIN_ANL(){
        select();
        move();
        select();
    }

    protected void selectSRH_OSD(){
        move();
        select(2);
    }

    protected void selectLDCU_RCPB(){
        move();
        select();
        move();
        select();
    }

    protected void selectWYBV_MWKJ(){
        move();
        select();
        move(2);
        select();
    }

    protected void selectMFPG_HUGV(){
        move(2);
        select(2);
    }

    protected void selectKXJQZ_FYXQZ(){
        move(2);
        select(1);
        move();
        select();
    }

    protected void selectToggleMode(){
        move(2);
        select();
        move(2);
        select();
    }

    protected void selectDictionary(){
        move(3);
        select(2);
    }

    /**
     * Should be used for choosing elements within tiles (toogle mode, dictionary and punctuation menu)
     * @param index selects index, starts at 0.
     */
    protected void chooseItemAt(int index){
        move(index);
        select();
    }

    protected void selectDeleteMenu(){
        select();
        move(2);
        select();
    }


    protected void fixWord(int times){
        selectDeleteMenu();
        select(times);
        move(2);
        select();
    }

    protected void deleteWord(int times){
        selectDeleteMenu();
        move();
        select(times);
        move();
        select();
    }

    protected void selectPunctuationSymbols(){
        move(3);
        select();
        move(1);
        select();
    }

    protected void selectPeriod(){
        selectPunctuationSymbols();
        chooseItemAt(0);
    }

    protected void selectComma(){
        selectPunctuationSymbols();
        chooseItemAt(1);
    }

    protected void selectQuestionMark(){
        selectPunctuationSymbols();
        chooseItemAt(2);
    }

    protected void selectExclamationMark(){
        selectPunctuationSymbols();
        chooseItemAt(3);
    }

    protected void selectSend(){
        move(3);
        select();
        move(2);
        select();
    }

    protected void move(){
        stepInput(InputType.INPUT1);
    }

    protected void move(int times){
        for (int i = 0; i < times; i++) {
            move();
        }
    }

    protected void select(){
        stepInput(InputType.INPUT2);
    }

    protected void select(int times){
        for (int i = 0; i < times; i++) {
            select();
        }
    }


    protected void assertExpectedHistorySize(int expected) {
        assertEquals(expected, layout.getDictionary().getWordHistorySize());
    }

    protected void assertExpectedNumberOfSuggestions(int expected){
        assertEquals(expected, layout.getSuggestions().size());
    }
}
