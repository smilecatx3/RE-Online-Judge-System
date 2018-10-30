package reojs.config;


public class ConfigException extends RuntimeException {
    static final int KEY_ERROR        = 0x1;
    static final int CONVERSION_ERROR = 0x1 << 1;


    static String getErrorMessage(String name, int type) {
        switch (type) {
            case KEY_ERROR:
                return String.format("The config does not contain such name: '%s'", name);
            case CONVERSION_ERROR:
                return String.format("The data value of '%s' is not of the type.", name);
            default:
                throw new IllegalArgumentException("No such type");
        }
    }

    ConfigException(String message) {
        super(message);
    }
}
