package reojs.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;


public class IniConfig implements Config {
    private Configuration data;


    public IniConfig(Path file) throws IOException, ConfigurationException {
        data = new INIConfiguration();
        ((INIConfiguration) data).read(new FileReader(file.toFile()));
    }

    @Override
    public String getString(String key) {
        checkKey(key);
        try {
            return data.getString(key);
        } catch (ConversionException e) {
            throw new ConfigException(conversionErrorMessage(key));
        }
    }

    @Override
    public int getInteger(String key) {
        checkKey(key);
        try {
            return data.getInt(key);
        } catch (ConversionException e) {
            throw new ConfigException(conversionErrorMessage(key));
        }
    }

    @Override
    public double getDouble(String key) {
        checkKey(key);
        try {
            return data.getFloat(key);
        } catch (ConversionException e) {
            throw new ConfigException(conversionErrorMessage(key));
        }
    }

    @Override
    public boolean getBoolean(String key) {
        checkKey(key);
        try {
            return data.getBoolean(key);
        } catch (ConversionException e) {
            throw new ConfigException(conversionErrorMessage(key));
        }
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    public void setProperty(String key, Object value) {
        data.setProperty(key, value);
    }

    @Override
    public void removeProperty(String key) {
        checkKey(key);
        data.clearProperty(key);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        var iterator = data.getKeys();
        while (iterator.hasNext()) {
            var key = iterator.next();
            s.append(String.format("%s = %s %n", key, data.getProperty(key)));
        }
        return s.toString();
    }

    private void checkKey(String name) {
        if (!data.containsKey(name)) {
            throw new ConfigException(ConfigException.getErrorMessage(name,
                                      ConfigException.KEY_ERROR));
        }
    }

    private String conversionErrorMessage(String name) {
        return ConfigException.getErrorMessage(name, ConfigException.CONVERSION_ERROR);
    }
}
