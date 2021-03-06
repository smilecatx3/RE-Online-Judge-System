package reojs.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import reojs.JudgeSystemException;
import reojs.Submission;
import reojs.config.Config;
import reojs.config.IniConfig;


public class JudgeSystem {
    private static JudgeSystem instance;
    private static final Log log = LogFactory.getLog(JudgeSystem.class);

    private Config config;
    private ExecutorService mainService = Executors.newSingleThreadExecutor();
    private ExecutorService judgeService;
    private BlockingQueue<Ticket> queue = new LinkedBlockingQueue<>();


    public static synchronized void initialize(Path config) throws Exception {
        if (instance == null) {
            instance = new JudgeSystem(config);
        }
    }

    public static synchronized void shutdown() {
        var system = getInstance();
        system.judgeService.shutdownNow();
        system.mainService.shutdownNow();
        instance = null;
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

    private JudgeSystem(Path configFile) throws Exception {
        config = new IniConfig(configFile);
        if (!config.has("system.working_dir")) {
            config.setProperty("system.working_dir", Files.createTempDirectory("reojs").toString());
        }
        log.info(String.format("System config '%s': %n%s", configFile, config));

        // Keep one processor free to prevent the server from being full loading
        int capacity = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
        judgeService = Executors.newFixedThreadPool(capacity);
        log.info("Available number of concurrent tasks: " + capacity);

        run();
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
