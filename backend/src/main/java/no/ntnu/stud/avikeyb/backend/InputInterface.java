package no.ntnu.stud.avikeyb.backend;

/**
 * Represents the interface input devices can use to communicate and send input signals to the keyboard
 */
public interface InputInterface {

    /**
     * Sends a input signal to the keyboard
     *
     * @param input the input signal type
     */
    void sendInputSignal(InputType input);

}
