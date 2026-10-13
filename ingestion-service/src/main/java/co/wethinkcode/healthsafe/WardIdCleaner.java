package co.wethinkcode.healthsafe;

import java.util.regex.Pattern;

public class WardIdCleaner {
    public static final Pattern VALID_ID = Pattern.compile("W-\\d+");

    public static String clean(String rawWardId){


        if (Placeholders.isPlaceholder(rawWardId)){
            throw new IllegalArgumentException("ERROR: Ward ID is required");
        }
        String wardId = rawWardId.trim().toUpperCase();
        return wardId;
    }
}
