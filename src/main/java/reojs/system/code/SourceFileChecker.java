package reojs.system.code;

import java.nio.file.Path;
import java.util.function.Function;


/**
 * The class examines if the submitted zip file content is correct.
 */
public interface SourceFileChecker extends Function<Path, Boolean> {
    /**
     * @param path the path to the directory where the zip file is extracted.
     * @return true if the zip file content is correct, otherwise false.
     */
    @Override
    Boolean apply(Path path);

    /**
     * Gets the hint message for those invalid source files.
     */
    String getHintMessage();
}
