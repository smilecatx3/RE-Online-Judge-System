package reojs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import reojs.JudgeSystemException;
import reojs.core.JudgeSystem;


public class SConsBuilder implements Builder {
    private static final Log log = LogFactory.getLog(SConsBuilder.class);
    private static final String PYTHON;
    private static final String SCONS;
    private static final long TIMEOUT;

    private Sconscript script;
    private Path workingDir;
    private String sconsOutput;


    static {
        var config = JudgeSystem.getConfig("scons");
        PYTHON = config.getString("python");
        SCONS = config.getString("scons");
        TIMEOUT = config.getLong("timeout");

        checkPathExists(PYTHON);
        checkPathExists(SCONS);

        log.info(String.format("SConsBuilder{Python: %s; Scons: %s; Timeout: %d}",
                               PYTHON, SCONS, TIMEOUT));
    }

    private static void checkPathExists(String path) {
        if (Files.notExists(Paths.get(path))) {
            throw new UncheckedIOException(new FileNotFoundException(path));
        }
    }


    public SConsBuilder(Sconscript script, Path workingDir) {
        this.script = script;
        this.workingDir = workingDir;
    }

    @Override
    public int build() throws Exception {
        script.write(workingDir);

        var commands = List.of(PYTHON, SCONS, "-C", workingDir.toString());
        var execResult = new CommandExecutor(TIMEOUT).execute(commands).orElse(null);
        if (execResult != null) {
            sconsOutput = execResult.getOutput();
            int exitValue = execResult.getExitValue();
            log.trace(String.format("exit value: %d; scons output: %s", exitValue, sconsOutput));
            return exitValue;
        } else {
            throw new JudgeSystemException("Failed to execute build commands.");
        }
    }

    @Override
    public String getBuildMessage() {
        if (sconsOutput == null) {
            throw new IllegalStateException();
        }
        return sconsOutput;
    }
}
