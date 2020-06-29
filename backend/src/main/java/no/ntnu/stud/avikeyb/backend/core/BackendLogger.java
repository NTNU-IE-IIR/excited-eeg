package no.ntnu.stud.avikeyb.backend.core;

/**
 * Created by Tor Martin Holen on 27-Mar-17.
 */
public class BackendLogger {
    private static Logger logger;

    public static void log(String message){
        if(logger != null){
            logger.log(message);
        }
    }

    public static void setLogger(Logger newLogger){
        logger = newLogger;
    }
}
