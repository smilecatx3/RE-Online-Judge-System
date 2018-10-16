package reojs;

/**
 * The exception indicates that the judge system failes to complete a judgement due to invalid
 * submission such as format error, uncompilable source code, execution timed out, and so forth.
 */
public class JudgeFailure extends RuntimeException {
    public static final int INVALID_SOURCE_FILE = 0x1;
    public static final int COMPILE_ERROR       = 0x1 << 1;
    public static final int EXECUTION_TIMEOUT   = 0x1 << 2;


    private int errorCode;

    public JudgeFailure(int cause, String message) {
        super(message);
        this.errorCode = cause;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
