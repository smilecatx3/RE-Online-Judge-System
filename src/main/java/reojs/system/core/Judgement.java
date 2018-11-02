package reojs.system.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import reojs.system.code.Compilable;
import reojs.system.code.Executable;
import reojs.system.JudgeSystemException;
import reojs.system.ProgressListener;
import reojs.system.Submission;


public class Judgement {
    private static final Log log = LogFactory.getLog(Judgement.class);
    private Submission submission;
    private ProgressListener listener;
    private TestSet testset;


    Judgement(Submission s) throws JudgeSystemException {
        submission = s;
        testset = new TestSet(submission.getProblemId());
        log.trace("Test set: " + testset);
    }

    public Submission getSubmission() {
        return submission;
    }

    public TestSet getTestSet() {
        return testset;
    }

    public ProgressListener getProgressListener() {
        if (listener == null) {
            listener = (progress, status) ->
                       System.out.println(String.format("[%.0f%%] %s", progress*100, status));
        }
        return listener;
    }

    public void addProgressListener(ProgressListener x) {
        listener = x;
    }

    JudgeReport start() throws Exception {
        var source = submission.getSourceCode();
        if (source instanceof Compilable) {
            // Compiled language goes here
            getProgressListener().onProgress(0.5, ProgressListener.STATUS_COMPILE);
            var executable = ((Compilable)source).compile();
            return new JudgeDelegate(this).execute(executable);
        } else {
            // Interpreted language goes here
            return new JudgeDelegate(this).execute((Executable)source);
        }
    }
}
