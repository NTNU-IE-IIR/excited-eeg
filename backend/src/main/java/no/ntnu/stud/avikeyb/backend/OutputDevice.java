package no.ntnu.stud.avikeyb.backend;

/**
 * Represents an output destination for the keyboard output
 * <p>
 * This can be anything, e.g. print to the console, send message to facebook etc.
 */
public interface OutputDevice {


    /**
     * Sends the output string to some destination
     *
     * @param output the output string to send
     */
    void sendOutput(String output);
}
