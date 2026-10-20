package co.wethinkcode.healthsafe;

import java.util.Set;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static java.lang.System.load;

public class Placeholders {

    public static final String RESOURCE = "/placeholders.txt";
    public static final Set<String> VALUES =load();


    private Placeholders() {
    }


    public static boolean isPlaceholder(String raw) {
        if (raw == null) {
            return true;
        }
        String normalised = raw.trim().toLowerCase();
        return normalised.isEmpty() || VALUES.contains(normalised);
    }

    private static Set<String> load() {
        try (InputStream in = Placeholders.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(RESOURCE + " not found on the classpath");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + RESOURCE, e);
        }
    }
}
