package reojs.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import reojs.system.core.JudgeSystem;


@WebListener
public class WebApplication implements ServletContextListener {
    private static final Log log = LogFactory.getLog(WebApplication.class);
    private boolean systemInitialized = false;


    public WebApplication() {
        System.setProperty("java.net.preferIPv4Stack" , "true");
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            var context = event.getServletContext();
            Path config = Paths.get(context.getRealPath("/WEB-INF/config.ini"));
            JudgeSystem.initialize(config);
            systemInitialized = true;
            log.info("Servlet context initialized.");
        } catch (Exception e) {
            log.fatal("Failed to initialize system.", e);
            System.exit(-1);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (systemInitialized) {
            JudgeSystem.shutdown();
            log.info("Servlet context destroyed");
        }
    }
}
