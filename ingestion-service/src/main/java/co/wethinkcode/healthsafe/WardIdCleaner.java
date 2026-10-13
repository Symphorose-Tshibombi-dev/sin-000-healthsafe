package co.wethinkcode.healthsafe;

import java.util.regex.Pattern;

public class WardIdCleaner {
    public static final Pattern VALID_ID = Pattern.compile("W-\\d+");

    public static String clean(String rawWardId){


        if (Placeholders.isPlaceholder(rawWardId)){
            throw new IllegalArgumentException("ERROR: Expected ward ID entry.");
        }
        String wardId = rawWardId.trim().toUpperCase();

        if (!VALID_ID.matcher(wardId).matches()) {
            throw new IllegalArgumentException("ERROR: Invalid ward ID format.");
        }
        return wardId;
    }
}
