package co.wethinkcode.healthsafe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BedAvailabilityCleaner {

    private static final String RESOURCE = "/limits.properties";
    private static final int MAX_BEDS = loadMaxBeds();

    public static Integer clean(String rawBedsAvailable){

        if(Placeholders.isPlaceholder(rawBedsAvailable)){
            return null;
        }

        String cleanedValue = rawBedsAvailable.trim();

        int bedsAvailable;

        try{ bedsAvailable = Integer.parseInt(cleanedValue);

        }catch(NumberFormatException exception){
            throw new IllegalArgumentException("ERROR: Expected numeric entry");
        }

        if(bedsAvailable < 0){
            throw new IllegalArgumentException("ERROR: Expected positive number");
        }
        if(bedsAvailable > MAX_BEDS){
            throw new IllegalArgumentException("ERROR: Number of beds is unrealistically large.");
        }
        return bedsAvailable;
    }

    public static int loadMaxBeds(){
        try(InputStream in = BedAvailabilityCleaner.class.getResourceAsStream(RESOURCE)){
            if (in == null){
                throw new IllegalArgumentException(RESOURCE + "not found on the classpath");
            }
            Properties properties = new Properties();
            properties.load(in);

            String value = properties.getProperty("max.beds");
            if (value == null) {
                throw new IllegalStateException("max.beds is missing in " + RESOURCE);
        }
            return Integer.parseInt(value.trim());
    }catch (IOException | NumberFormatException exception){
        throw new IllegalStateException("Could not read max.beds from " + RESOURCE, exception);
        }
    }
}
