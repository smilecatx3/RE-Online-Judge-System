package reojs;

import java.util.List;
import java.util.Optional;


public interface Executable {
    /**
     * Gets the program entry point; null is returned if it cannot be found.
     */
    String getEntryPoint();

    /**
     * Gets the options for executing the program.
     */
    Optional<List<String>> getExecOptions();
}
