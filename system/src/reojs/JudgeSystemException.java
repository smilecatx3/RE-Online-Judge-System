package reojs;


/**
 * Signals that an internal error occurred in the judge system.
 */
public class JudgeSystemException extends Exception {
    public JudgeSystemException(String message) {
        super(message);
    }

    public JudgeSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
