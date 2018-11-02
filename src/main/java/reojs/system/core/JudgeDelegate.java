package reojs.system.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import reojs.system.code.Executable;
import reojs.system.JudgeFailure;
import reojs.system.JudgeSystemException;
import reojs.system.code.Portable;
import reojs.system.ProgressListener;
import reojs.system.util.CommandExecutor;


class JudgeDelegate {
    private static final Log log = LogFactory.getLog(JudgeDelegate.class);

    private Judgement judgement;
    private ProgressListener listener;
    private CommandExecutor executor;


    JudgeDelegate(Judgement j) {
        judgement = j;
        listener = judgement.getProgressListener();
        executor = new CommandExecutor(judgement.getTestSet().getTimeout());
    }

    JudgeReport execute(Executable e) throws Exception {
        List<String> commands = new ArrayList<>();

        if (e instanceof Portable) {
            var p = (Portable)e;
            commands.add(p.getExecutor().toString());
            p.getExecOptions().ifPresent(commands::addAll);
        }

        var entryPoint = e.getEntryPoint();
        if (entryPoint == null) {
            // We believe the error is due to invalid submission file which implies not the system
            // fault, so we throw JudgeFailure rather than JudgeSystemException.
            throw new JudgeFailure(JudgeFailure.INVALID_SOURCE_FILE,
                                   "Cannot find the program entry point.");
        }
        commands.add(entryPoint);

        return execute(commands);
    }

    private JudgeReport execute(List<String> cmd) throws Exception {
        var testCases = judgement.getTestSet().getTestCases();
        JudgeResult[] results = new JudgeResult[testCases.size()];
        long elapsedTime = 0; // Prevent a judgement occupies resource too long
        long timeout = JudgeSystem.getConfig().getInteger("system.timeout");

        for (int i=0; i<results.length; i++) {
            listener.onProgress(0.5+((i+1.0)/results.length)*0.5, ProgressListener.STATUS_EXECUTE);
            var commands = appendProgramInputs(cmd, testCases.get(i).getInputs());
            var testCase = testCases.get(i);
            JudgeResult result = executeCommand(commands, testCase);
            elapsedTime += result.getRuntime();
            if (elapsedTime >= timeout) {
                throw new JudgeFailure(JudgeFailure.EXECUTION_TIMEOUT, "Time limit exceeded");
            }
            results[i] = result;
            log(testCase, result);
        }

        return new JudgeReport(judgement, results);
    }

    private List<String> appendProgramInputs(List<String> commands, List<String> inputs)
                         throws IOException {
        if (judgement.getTestSet().isStdin()) {
            redirectInput(inputs);
            return commands;
        } else {
            var stream1 = commands.stream();
            var stream2 = inputs.stream().map((item) -> item.replace("\"", "\\\""));
            return Stream.concat(stream1, stream2).collect(Collectors.toList());
        }
    }

    private void redirectInput(List<String> inputs) throws IOException {
        var workingDir = JudgeSystem.getConfig().getString("system.working_dir");
        Path file = Files.createTempFile(Paths.get(workingDir), null, null);
        Files.write(file, String.join("\n", inputs).getBytes(StandardCharsets.UTF_8));
        executor.redirectInput(file.toFile());
    }

    private JudgeResult executeCommand(List<String> commands, TestCase testCase)
                                       throws JudgeSystemException {
        try {
            var execResult = executor.execute(commands).orElse(null);
            if (execResult != null) {
                return new JudgeResult(execResult.getOutput(), execResult.getRuntime(), testCase);
            } else {
                throw new JudgeSystemException("Failed to execute judge command.");
            }
        } catch (TimeoutException e) {
            return new JudgeResult(null, executor.getTimeout(), testCase);
        }
    }

    private void log(TestCase t, JudgeResult j) {
        if (log.isTraceEnabled()) {
            log.trace(String.format("Test case: %s; Judge result: %s", t, j));
        }
    }
}
