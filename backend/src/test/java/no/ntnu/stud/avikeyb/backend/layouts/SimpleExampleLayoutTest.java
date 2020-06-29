package no.ntnu.stud.avikeyb.backend.layouts;


import org.junit.Test;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Layout;

/**
 * Created by pitmairen on 08/02/2017.
 */
public class SimpleExampleLayoutTest extends LayoutTestBase {

    @Override
    protected Layout createLayout() {
        return new SimpleExampleLayout(keyboard);
    }

    @Test
    public void testTyping() throws Exception {

        assertOutputBufferEquals("");

        // Move to letter "c" and select
        stepInput(InputType.INPUT2);
        stepInput(InputType.INPUT2);
        stepInput(InputType.INPUT2);
        stepInput(InputType.INPUT1);


        assertOutputBufferEquals("c");


        // Move to letter "a" and select
        stepInput(InputType.INPUT2);
        stepInput(InputType.INPUT1);

        assertOutputBufferEquals("ca");

        // Send button
        stepInput(InputType.INPUT1);

        assertLastOutputEquals("ca");
        assertOutputBufferEquals("");
    }

}