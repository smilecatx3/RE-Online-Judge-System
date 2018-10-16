package reojs.core;

import java.util.Objects;


/**
 * A JudgeResult is the execution result for each test case.
 */
public class JudgeResult {
    private TestCase testCase;
    private String answer;
    private long runtime;
    private boolean isTimedout;


    /**
     * @param answer null if the execution is timed out.
     * @param runtime the execution time in milliseconds.
     * @throws NullPointerException if testCase is null.
     */
    JudgeResult(String answer, long runtime, TestCase testCase) {
        this.answer = Objects.requireNonNullElse(answer, "N/A");
        this.runtime = runtime;
        this.testCase = Objects.requireNonNull(testCase);
        this.isTimedout = (answer == null);
    }

    public TestCase getTestCase() {
        return testCase;
    }

    /** Gets the program output of the submitted code. */
    public String getAnswer() {
        return answer;
    }

    /** Gets the execution time in milliseconds.  */
    public long getRuntime() {
        return runtime;
    }

    public boolean isTimedout() {
        return isTimedout;
    }

    public boolean isPassed() {
        if (isTimedout) {
            return false;
        }
        String yourOutput = normalize(answer);
        String expectedOutput = normalize(testCase.getOutput());
        return yourOutput.equals(expectedOutput);
    }

    private String normalize(String str) {
        return str.replace("\r", "").trim();
    }

    @Override
    public String toString() {
        return String.format("JudgeResult{testCase: %s; answer: %s; runtime: %d; isTimedout: %b}",
                             testCase, answer, runtime, isTimedout);
    }
}
