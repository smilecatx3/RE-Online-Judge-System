package reojs.system.core;

import java.util.Arrays;
import java.util.List;


public class JudgeReport {
    private Judgement judgement;
    private List<JudgeResult> judgeResults;
    private int numPassed;
    private long elapsedTime;


    JudgeReport(Judgement j, JudgeResult[] results) {
        this.judgement = j;
        this.judgeResults = Arrays.asList(results);
        this.numPassed = (int)judgeResults.stream().filter(JudgeResult::isPassed).count();
        this.elapsedTime = judgeResults.stream().mapToLong(JudgeResult::getRuntime).sum();
    }

    public Judgement getJudgement() {
        return judgement;
    }

    public List<JudgeResult> getJudgeResults() {
        return judgeResults;
    }

    /** Gets the elapsed time of a judgement in milliseconds. */
    public long getElapsedTime() {
        return elapsedTime;
    }

    public long getNumPassed() {
        return numPassed;
    }

    /**
     * @param base the base score in the range between 0 and 100.
     * @return the score of the judgement that is greater or equal to the given base score.
     *         The latter case indicates getNumPassed() is 0.
     * @throws IllegalArgumentException if base is not in the range between 0 and 100.
     */
    public int getScore(int base) {
        if (base < 0 || base > 100) {
            throw new IllegalArgumentException("base should be in the range [0, 100]");
        }
        return (int)Math.ceil(base + ((double)numPassed/judgeResults.size())*(100-base));
    }

    @Override
    public String toString() {
        return String.format("JudgeReport{judgeResults: %s; numPassed: %d; elapsedTime: %d}",
                             judgeResults, numPassed, elapsedTime);
    }
}
