package eu.vandertil.jerasure;

/**
 * This class loads the native library or throws an exception.
 *
 * @author Jos van der Til
 * @version 1.0
 * @since 1.0
 */
public class LibraryLoader {
    public static void load() {
        System.loadLibrary("Jerasure.jni");
        //System.out.println("Correctly loaded libJerasure.jni");
    }
}
