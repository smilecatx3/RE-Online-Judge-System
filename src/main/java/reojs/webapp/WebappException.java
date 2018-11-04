package reojs.webapp;


public class WebappException extends Exception {
    WebappException(String message) {
        super(message);
    }

    WebappException(String message, Throwable cause) {
        super(message, cause);
    }
}
