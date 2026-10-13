package co.wethinkcode.healthsafe;

public class BedAvailabilityCleaner {

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
        return bedsAvailable;
    }
}
