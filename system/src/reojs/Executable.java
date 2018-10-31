package reojs;


public interface Executable {
    /**
     * Gets the program entry point; null is returned if it cannot be found.
     */
    String getEntryPoint();
}
