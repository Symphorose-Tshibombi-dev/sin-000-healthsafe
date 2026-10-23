package co.wethinkcode.healthsafe;

import java.io.*;
import java.util.Properties;

public class ServiceConfig {

    public static String ingestionUrl() {
                try (InputStream in = ServiceConfig.class.getResourceAsStream("/service.properties")) {
                    Properties properties = new Properties();
                    properties.load(in);
                    return properties.getProperty("ingestion.url");
                }catch (IOException error) {
                    throw new IllegalStateException("Could not read service.properties", error);
                }
        }
}
