package reojs.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.PreDestroy;

import reojs.system.core.JudgeSystem;


@SpringBootApplication(scanBasePackages={"reojs.system", "reojs.webapp"})
public class WebApplication {
    private static final Log log = LogFactory.getLog(WebApplication.class);


    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    public WebApplication() {
        log.info("Start web application ...");
    }

    @PreDestroy
    private void destroy() {
        log.info("Shutdown web application ...");
        JudgeSystem.shutdown();
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }
}
