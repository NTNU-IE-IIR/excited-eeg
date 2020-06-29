package no.ntnu.stud.avikeyb.backend;

/**
 * Interface representing the current state of the keyboard.
 * <p>
 * The keyboard holds an output buffer that is gradually filled as the users types characters. When the user
 * has added the desired input to the buffer the keyboard can send the current buffer to somewhere.
 */
public interface Keyboard {

    /**
     * Listen for changes in the internal state of the keyboard
     */
    interface KeyboardListener {
        /**
         * Called when the output buffer changes
         *
         * @param oldBuffer the old value of the buffer
         * @param newBuffer the new value of the buffer
         */
        void onOutputBufferChange(String oldBuffer, String newBuffer);
    }


    /**
     * Listen for request for keybaord settings
     */
    interface SettingsListener {
        /**
         * Called when the user want to change settings
         */
        void onSettingsRequestd();
    }


    /**
     * Adds the string value to the current keyboard output buffer
     *
     * @param value
     */
    void addToCurrentBuffer(String value);

    /**
     * Should send the current buffer to some kind output destination
     */
    void sendCurrentBuffer();


    /**
     * Returns the last word typed or currently being typed in the current output buffer
     *
     * @return the last word in the output buffer, can be an empty string or the start of a partially typed word
     */
    String getCurrentWord();

    /**
     * Returns the whole content of the current output buffer
     *
     * @return the content of the current output buffer
     */
    String getCurrentBuffer();

    /**
     * Clears the current output buffer and reset it to a empty string
     */
    void clearCurrentBuffer();


    /**
     * Removes the last typed word from the output buffer
     */
    void deleteLastWord();


    /**
     * Removes the last typed character from the output buffer
     */
    void deleteLastCharacter();

    /**
     * Register an output device to the keyboard
     *
     * @param output the output device to register
     */
    void addOutputDevice(OutputDevice output);

    /**
     * Removes a registered output device from the keyboard
     *
     * @param output the output device to unregister
     */
    void removeOutputDevice(OutputDevice output);

    /**
     * Register an listener for the keyboard state
     *
     * @param listener the output device to register
     */
    void addStateListener(KeyboardListener listener);

    /**
     * Removes a registered listener from the keyboard
     *
     * @param listener the output device to unregister
     */
    void removeStateListener(KeyboardListener listener);


    /**
     * Should be called to let the user change any settings the keyboard may have
     */
    void requestChangeSettings();

    /**
     * Register an listener when the user requests to change settings
     *
     * @param listener the settings listener to register
     */
    void addSettingsListener(SettingsListener listener);

    /**
     * Removes a registered listener from the keyboard
     *
     * @param listener the settings listener to unregister
     */
    void removeSettingsListener(SettingsListener listener);

}
