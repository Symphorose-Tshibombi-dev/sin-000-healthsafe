package co.wethinkcode.healthsafe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads the addresses of the other services from service.properties,
 * so they are not written into the code.
 */
public class ServiceConfig {

    private static final String RESOURCE = "/service.properties";

    private ServiceConfig() {
    }

    public static String wardUrl() {
        return read("ward.url");
    }

    public static String alertLevelUrl() {
        return read("alert-level.url");
    }

    private static String read(String key) {
        try (InputStream in = ServiceConfig.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(RESOURCE + " not found on the classpath");
            }
            Properties properties = new Properties();
            properties.load(in);

            String value = properties.getProperty(key);
            if (value == null || value.isBlank()) {
                throw new IllegalStateException(key + " is missing in " + RESOURCE);
            }
            return value.trim();
        } catch (IOException error) {
            throw new IllegalStateException("Could not read " + RESOURCE, error);
        }
    }
}