package reojs.config;

/**
 * The interface is a facade for accessing config data, with four primitive data types: string,
 * integer, double and boolean. The getter methods will throw ConfigException if the config does
 * not contain the given key, or the mapped value is not of that type. <p>
 *
 * For the structural config data,  use "." to access child elements; for example, if the config
 * data is of INI file format, which contains sections, use "section.key" to access the property
 * named "key" in the section named "section".
 */
public interface Config {
    boolean has(String key);

    String getString(String key);

    int getInteger(String key);

    double getDouble(String key);

    boolean getBoolean(String key);

    /**
     * Sets a property. The old property will be overriden by the new one if the key is already
     * present in the config.
     *
     * @throws UnsupportedOperationException if the config is immutable.
     */
    default void setProperty(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a property.
     *
     * @throws ConfigException if there is no such key in the config.
     * @throws UnsupportedOperationException if the config is immutable.
     */
    default void removeProperty(String key) {
        throw new UnsupportedOperationException();
    }
}
