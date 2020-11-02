package no.ntnu.stud.avikeyb.backend.layouts;

import org.junit.Before;

import no.ntnu.stud.avikeyb.backend.InputType;
import no.ntnu.stud.avikeyb.backend.Keyboard;
import no.ntnu.stud.avikeyb.backend.Layout;
import no.ntnu.stud.avikeyb.backend.core.CoreKeyboard;
import no.ntnu.stud.avikeyb.backend.outputs.OutputLogger;

import static org.junit.Assert.assertEquals;

/**
 * Created by pitmairen on 2/10/17.
 */
public abstract class LayoutTestBase {

    protected Keyboard keyboard;
    protected OutputLogger outputLogger;
    protected Layout layout;

    @Before
    public void setUp() throws Exception {
        keyboard = new CoreKeyboard();
        layout = createLayout();
        outputLogger = new OutputLogger();
        keyboard.addOutputDevice(outputLogger);
    }

    /**
     * Returns the layout that should be tested
     *
     * @return the layout object to be tested
     */
    protected abstract Layout createLayout();

    /**
     * Assert that the last keyboard output is as expected
     *
     * @param expected the expected output
     */
    protected void assertLastOutputEquals(String expected) {
        assertEquals(expected, outputLogger.getLastOutput());
    }


    /**
     * Assert that the keyboard output buffer is as expected
     *
     * @param expected the expected buffer content
     */
    protected void assertOutputBufferEquals(String expected) {
        assertEquals(expected, keyboard.getCurrentBuffer());
    }

    /**
     * Send a single step signal of the given input type to the layout
     * <p>
     * A step is a single cycle from on to off of the given signal
     *
     * @param inputType the signal type to send to the layout
     */
    protected void stepInput(InputType inputType) {
        layout.sendInputSignal(inputType);
    }
}
