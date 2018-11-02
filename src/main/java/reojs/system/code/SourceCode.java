package reojs.system.code;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import reojs.system.JudgeFailure;
import reojs.system.JudgeSystemException;
import reojs.system.core.JudgeSystem;


/**
 * The class represents the source code submitted to the judge system. The underlying content of an
 * instance may be a plain text file, or a directory contains the files extracted from a zip file.
 * The subclasses should either implement Compilable interface (for compiled languages) or
 * Portable interface (for interpreted languages), and handle how the source code is compiled and
 * executed for both two types of the underlying content.
 */
public abstract class SourceCode {
    private static final Log log = LogFactory.getLog(SourceCode.class);

    protected Path workingDir; // Created in initialize()
    private Path file;
    private Path directory;


    /**
     * Constructs a new SourceCode with the underlying content being a plain text file.
     */
    public SourceCode(String text) throws JudgeSystemException {
        this();
        file = createFile(text);
    }

    /**
     * Subclasses can define how the source code file is created. By default implementation, a
     * temp file is created inside the working directory, and the source code text provided by the
     * client is written to that file.
     *
     * @return the path to the created source code file.
     */
    protected Path createFile(String text) throws JudgeSystemException {
        try {
            var file = Files.createTempFile(workingDir, null, null);
            return Files.write(file, text.getBytes());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new JudgeSystemException("Failed to create a file from source text.");
        }
    }

    /**
     * Constructs a new SourceCode with the underlying content being a directory contains the files
     * extracted from the provided zip file. In absence of a checker to examine if the zip file
     * content is correct, by default the system assumes it is always correct. Subclasses can
     * override this behavior by providing a default checker implementation.
     */
    public SourceCode(Path zipFile) throws JudgeSystemException {
        this(zipFile, new SourceFileChecker() {
            @Override
            public Boolean apply(Path path) {
                return true;
            }
            @Override
            public String getHintMessage() {
                throw new UnsupportedOperationException("The method should never be called.");
            }
        });
    }

    /**
     * Constructs a new SourceCode with the underlying content being a directory contains the files
     * extracted from the provided zip file, and with a checker to examine if the zip file content
     * is correct.
     *
     * @param checker Given a path to the directory where the zip file is extracted, returns true
     *                if the zip file content is correct, otherwise false.
     */
    public SourceCode(Path zipFile, SourceFileChecker checker) throws JudgeSystemException {
        this();
        directory = extractZipFile(zipFile);
        if (!checker.apply(directory)) {
            throw new JudgeFailure(JudgeFailure.INVALID_SOURCE_FILE, checker.getHintMessage());
        }
    }

    /**
     * Subclasses can define how the zip file is extracted. By default implementation, the zip file
     * is just extracted to the working directory.
     *
     * @return The path to the directory to which the zip file is extracted.
     */
    protected Path extractZipFile(Path zipFile) throws JudgeSystemException {
        log.trace(String.format("Extract zip file '%s' to '%s'", zipFile, workingDir));
        try {
            new ZipFile(zipFile.toFile()).extractAll(workingDir.toString());
            return workingDir;
        } catch (ZipException e) {
            log.error(e.getMessage(), e);
            throw new JudgeSystemException("Failed to extract zip file.");
        }
    }

    private SourceCode() throws JudgeSystemException {
        try {
            var wd = JudgeSystem.getConfig().getString("system.working_dir");
            workingDir = Files.createTempDirectory(Paths.get(wd), null);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new JudgeSystemException("Failed to create a temp directory.");
        }
    }

    public Path getWorkingDir() {
        return workingDir;
    }

    /**
     * Returns the path to the source code file if the submission is a single file, otherwise
     * null is returned.
     */
    public Optional<Path> getFile() {
        return Optional.ofNullable(file);
    }

    /**
     * Returns the path to the directory where the submitted zip file is extracted. If the
     * submission is a single file then null is returned.
     */
    public Optional<Path> getDirectory() {
        return Optional.ofNullable(directory);
    }
}
