package reojs.system.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import reojs.system.JudgeSystemException;
import reojs.system.Submission;
import reojs.system.config.Config;
import reojs.system.config.IniConfig;


@Service
public class JudgeSystem {
    private static final Log log = LogFactory.getLog(JudgeSystem.class);
    private static JudgeSystem instance;

    private Config config;
    private ExecutorService mainService = Executors.newSingleThreadExecutor();
    private ExecutorService judgeService;
    private BlockingQueue<Ticket> queue = new LinkedBlockingQueue<>();


    public JudgeSystem(@Value("${system.config}") String configFile) throws Exception {
        config = new IniConfig(Paths.get(configFile));
        if (!config.has("system.working_dir")) {
            config.setProperty("system.working_dir", Files.createTempDirectory("reojs").toString());
        }
        log.info(String.format("System config '%s': %n%s", configFile, config));

        // Keep one processor free to prevent the server from being full loading
        int capacity = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
        judgeService = Executors.newFixedThreadPool(capacity);
        log.info("Available number of concurrent tasks: " + capacity);

        run();
        JudgeSystem.instance = this;
        log.info("The judge system has been initialized successfully.");
    }

    public static synchronized void shutdown() {
        var system = getInstance();
        system.judgeService.shutdownNow();
        system.mainService.shutdownNow();
        instance = null;
        log.info("The judge system has been shutdown.");
    }

    public static Optional<Ticket> newTicket(Submission s) {
        String id = String.format("%.10f", Math.random()).substring(2);
        Ticket ticket = null;
        try {
            log.debug("New ticket: " + id);
            ticket = new Ticket(id, s);
        } catch (JudgeSystemException e) {
            log.error("Failed to create a ticket.", e);
        }
        return Optional.ofNullable(ticket);
    }

    public static Config getConfig() {
        return getInstance().config;
    }

    static void schedule(Ticket t) {
        getInstance().queue.offer(t);
        log.trace("Schedule ticket " + t.getId());
    }

    private static JudgeSystem getInstance() {
        if (instance != null) {
            return instance;
        }
        throw new IllegalStateException("The system has not been initialized yet.");
    }

    private void run() {
        mainService.submit(() -> {
            while (!judgeService.isShutdown()) {
                try {
                    var ticket = queue.take();
                    Callable<JudgeReport> task = () -> ticket.getJudgement().start();
                    ticket.onTaskSubmitted(judgeService.submit(task));
                    log.trace("Submitted task of the ticket " + ticket.getId());
                } catch (InterruptedException e) {
                    log.error("The take operation is interrupted.", e);
                }
            }
        });
    }
}
