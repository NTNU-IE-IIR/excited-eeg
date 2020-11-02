package no.ntnu.stud.avikeyb.backend.layouts;

import org.junit.Before;
import org.junit.Test;

import no.ntnu.stud.avikeyb.backend.InputType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by pitmairen on 08/02/2017.
 */
public class LayoutStepTest {


    private StepLayoutImpl layout;


    @Before
    public void setUp() throws Exception {
        layout = new StepLayoutImpl();

    }

    @Test
    public void onStep() throws Exception {

        assertNull(layout.getLastInput());

        assertNotEquals(InputType.INPUT1, layout.getLastInput());
        layout.sendInputSignal(InputType.INPUT1);
        assertEquals(InputType.INPUT1, layout.getLastInput());
        assertNotEquals(InputType.INPUT2, layout.getLastInput());
        assertNotEquals(InputType.INPUT3, layout.getLastInput());
        assertNotEquals(InputType.INPUT4, layout.getLastInput());

        assertNotEquals(InputType.INPUT2, layout.getLastInput());
        layout.sendInputSignal(InputType.INPUT2);
        assertNotEquals(InputType.INPUT1, layout.getLastInput());
        assertEquals(InputType.INPUT2, layout.getLastInput());
        assertNotEquals(InputType.INPUT3, layout.getLastInput());
        assertNotEquals(InputType.INPUT4, layout.getLastInput());

        assertNotEquals(InputType.INPUT3, layout.getLastInput());
        layout.sendInputSignal(InputType.INPUT3);
        assertNotEquals(InputType.INPUT1, layout.getLastInput());
        assertNotEquals(InputType.INPUT2, layout.getLastInput());
        assertEquals(InputType.INPUT3, layout.getLastInput());
        assertNotEquals(InputType.INPUT4, layout.getLastInput());

        assertNotEquals(InputType.INPUT4, layout.getLastInput());
        layout.sendInputSignal(InputType.INPUT4);
        assertNotEquals(InputType.INPUT1, layout.getLastInput());
        assertNotEquals(InputType.INPUT2, layout.getLastInput());
        assertNotEquals(InputType.INPUT3, layout.getLastInput());
        assertEquals(InputType.INPUT4, layout.getLastInput());
    }


    private static class StepLayoutImpl extends BaseLayout {

        private InputType lastInput;

        @Override
        protected void onStep(InputType input) {
            lastInput = input;
        }

        public InputType getLastInput() {
            return lastInput;
        }
    }
}