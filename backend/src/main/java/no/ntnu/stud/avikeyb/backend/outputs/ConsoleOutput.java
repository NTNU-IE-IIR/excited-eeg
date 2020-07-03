package no.ntnu.stud.avikeyb.backend.outputs;


import no.ntnu.stud.avikeyb.backend.OutputDevice;

/**
 * Sends the keybard output to the stdout
 */
public class ConsoleOutput implements OutputDevice {

    @Override
    public void sendOutput(String output) {
        System.out.println(output);
    }
}
