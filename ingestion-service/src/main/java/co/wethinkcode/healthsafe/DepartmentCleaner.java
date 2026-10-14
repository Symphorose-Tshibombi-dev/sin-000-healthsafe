package co.wethinkcode.healthsafe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.System.load;

public class DepartmentCleaner {

    private static final String RESOURCE = "/departments.properties";
    private static final Properties DEPARTMENTS = load();

    public static String clean(String rawDepartmentName){


        if(Placeholders.isPlaceholder(rawDepartmentName)){
           return null;
        }

        String key = rawDepartmentName.trim().toLowerCase();


        String mapped = DEPARTMENTS.getProperty(key);
        if(mapped != null){
            return mapped;
        }

        String[] departmentName = rawDepartmentName.toLowerCase().trim().split("\\s+");

        for(int i = 0;i < departmentName.length;i++ ){
            departmentName[i] =
                    departmentName[i].substring(0, 1).toUpperCase() +
                            departmentName[i].substring(1).toLowerCase();
        }
        return String.join(" " ,departmentName);
    }

    private static Properties load() {
        try (InputStream in = DepartmentCleaner.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(RESOURCE + " not found on the classpath");
            }
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + RESOURCE, e);
        }
    }



}
