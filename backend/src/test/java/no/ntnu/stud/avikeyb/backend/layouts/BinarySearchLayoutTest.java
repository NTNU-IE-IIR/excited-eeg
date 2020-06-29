package no.ntnu.stud.avikeyb.backend.layouts;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Layout;
import org.junit.Test;

/**
 * Created by pitmairen on 08/02/2017.
 */
public class BinarySearchLayoutTest extends LayoutTestBase {

    @Override
    protected Layout createLayout() {
        return new BinarySearchLayout(keyboard);
    }

    @Test
    public void testSelectLetters() throws Exception {

        assertOutputBufferEquals("");
        assertLastOutputEquals("");

        // a
        goLeft();
        goLeft();
        goLeft();
        goLeft();
        goLeft();
        goLeft();

        assertOutputBufferEquals("a");
        assertLastOutputEquals("");

        // h
        goLeft();
        goLeft();
        goRight();
        goLeft();
        goRight();
        goRight();

        assertOutputBufferEquals("ah");
        assertLastOutputEquals("");

        // a
        goLeft();
        goLeft();
        goLeft();
        goLeft();
        goLeft();
        goLeft();

        assertOutputBufferEquals("aha");
        assertLastOutputEquals("");

        // Send
        goLeft();
        goRight();
        goRight();
        goRight();
        goRight();

        assertOutputBufferEquals("");
        assertLastOutputEquals("aha");
    }


    private void goLeft() {
        stepInput(InputType.INPUT1);
    }

    private void goRight() {
        stepInput(InputType.INPUT2);
    }


}