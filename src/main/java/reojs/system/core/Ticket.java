package reojs.system.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import reojs.system.JudgeFailure;
import reojs.system.JudgeSystemException;
import reojs.system.Submission;


public class Ticket {
    private static final Log log = LogFactory.getLog(Ticket.class);

    private String id;
    private Judgement judgement;
    private BlockingQueue<Future<JudgeReport>> queue = new ArrayBlockingQueue<>(1);


    Ticket(String id, Submission s) throws JudgeSystemException {
        this.id = id;
        this.judgement = new Judgement(s);
    }

    public Optional<JudgeReport> submit() throws JudgeFailure {
        JudgeSystem.schedule(this);
        JudgeReport report = null;
        try {
            report = queue.take().get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.error("Failed to get judge report.", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof JudgeFailure) {
                throw (JudgeFailure)e.getCause();
            }
            log.error("Failed to complete the judgement.", e);
        }
        return Optional.ofNullable(report);
    }

    void onTaskSubmitted(Future<JudgeReport> f) {
        if(!queue.offer(f)) {
            throw new RuntimeException("Ivalid usage"); // Should never happen
        }
    }

    public String getId() {
        return id;
    }

    public Judgement getJudgement() {
        return judgement;
    }
}
