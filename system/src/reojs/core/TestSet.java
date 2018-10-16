package reojs.core;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import reojs.JudgeSystemException;


public class TestSet {
    private Path file;
    private List<TestCase> testcases = new ArrayList<>();
    private boolean stdin;
    private long timeout;


    TestSet(String problemId) throws JudgeSystemException {
        var testsetDir = JudgeSystem.getConfig("system").getString("testset_dir");
        file = Paths.get(testsetDir, problemId + ".json");
        try {
            var testset = new JSONObject(new String(Files.readAllBytes(file)));
            int length = testset.getJSONArray("inputs").length(); // Assert inputs.length equals outputs.length
            for (int i=0; i<length; i++) {
                testcases.add(createTestCase(testset, i));
            }
            stdin = testset.has("stdin") && testset.getBoolean("stdin");
            timeout = testset.has("timeout") ? testset.getLong("timeout") :
                    JudgeSystem.getConfig("system").getLong("timeout");
        } catch (IOException e) {
            throw new JudgeSystemException(
                    String.format("Failed to create a testset from '%s'", file.getFileName()), e);
        }
    }

    private TestCase createTestCase(JSONObject testset, int index) {
        var x = testset.getJSONArray("inputs").getJSONArray(index).spliterator();
        var inputs = StreamSupport.stream(x, false)
                                  .map(Object::toString).collect(Collectors.toList());
        var output = testset.getJSONArray("outputs").getString(index);
        return new TestCase(inputs, output);
    }

    public List<TestCase> getTestCases() {
        return testcases;
    }

    /**
     * @return true if the judgement uses stdin as program input, otherwise false.
     */
    public boolean isStdin() {
        return stdin;
    }

    /**
     * Gets the timeout for each test case. The default value is set as the judge
     * system timeout if the timeout is not sepcified in the test set file.
     */
    public long getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return String.format("TestSet{file: %s; testcases: %s; stdin: %b; timeout: %d}",
                             file, testcases, stdin, timeout);
    }
}
