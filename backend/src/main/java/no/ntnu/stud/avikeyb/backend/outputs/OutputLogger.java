package no.ntnu.stud.avikeyb.backend.outputs;


import no.ntnu.stud.avikeyb.backend.OutputDevice;

/**
 * Simple output implementation used for testing and debugging
 */
public class OutputLogger implements OutputDevice {

    // Stores the last string sent
    private String log = "";

    @Override
    public void sendOutput(String output) {
        log = output;
    }


    /**
     * Returns the last sent string
     *
     * @return the last sent string
     */
    public String getLastOutput() {
        return log;
    }

    /**
     * Reset the current state and set the last sent string to an empty string
     */
    public void reset() {
        log = "";
    }
}
