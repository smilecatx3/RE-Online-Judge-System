package reojs;


public interface Executable {
    /**
     * @return the program entry point, or null if the entry point cannot be found.
     */
    String getEntryPoint();
}
