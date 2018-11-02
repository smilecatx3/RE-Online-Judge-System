package reojs.system.code;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;


public interface Portable extends Executable {
    /**
     * Gets the path to the executor to execute the portable code.
     */
    Path getExecutor();

    /**
     * Gets the executor options for portable code execution.
     */
    Optional<List<String>> getExecOptions();
}
