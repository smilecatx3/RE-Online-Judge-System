package reojs.system.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class CommandExecutor {
    private static final Log log = LogFactory.getLog(CommandExecutor.class);

    private long timeout;
    private ProcessBuilder processBuilder;


    public CommandExecutor(long timeout) {
        this.timeout = Math.max(0, timeout);
        processBuilder = new ProcessBuilder().redirectErrorStream(true);
    }

    public Optional<ExecutionResult> execute(List<String> commands) throws TimeoutException {
        log(commands);

        ExecutionResult result = null;
        Process process = null;
        Timer timer = new Timer();

        try {
            timer.start();
            process = processBuilder.command(commands).start();
            if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException();
            }
            timer.stop();
            result = new ExecutionResult(
                    process.exitValue(),
                    IOUtils.toString(process.getInputStream(), getSystemEncoding()),
                    timer.getElapsedTime(TimeUnit.MILLISECONDS));
            log.trace(result);
        } catch (IOException | InterruptedException e) {
            log.error("Error occurred while executing commands.", e);
        } finally {
            assert (process != null);
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }

        return Optional.ofNullable(result);
    }

    public long getTimeout() {
        return timeout;
    }

    /**
     * @param file the new standard input source.
     */
    public void redirectInput(File file) {
        processBuilder.redirectInput(file);
    }

    private Charset getSystemEncoding() {
        return SystemUtils.IS_OS_WINDOWS ? Charset.forName("BIG5") : StandardCharsets.UTF_8;
    }

    private void log(List<String> commands) {
        if (log.isTraceEnabled()) {
            log.trace(String.format("Commands: %s; Timeout: %d; Redirect: %s",
                    commands, timeout, processBuilder.redirectInput().file()));
        }
    }


    public class ExecutionResult {
        private int exitValue;
        private String output;
        private long runtime;

        private ExecutionResult(int exitValue, String output, long runtime) {
            this.exitValue = exitValue;
            this.output = output;
            this.runtime = runtime;
        }

        public int getExitValue() {
            return exitValue;
        }

        public String getOutput() {
            return output;
        }

        public long getRuntime() {
            return runtime;
        }

        @Override
        public String toString() {
            return String.format("Exit value: %d; Output: %s; Runtime: %d",
                                 exitValue, output, runtime);
        }
    }
}
