package co.wethinkcode.healthsafe;

public class BedAvailabilityCleaner {

    public static Integer clean(String rawBedsAvailable){

        if(rawBedsAvailable.trim().equalsIgnoreCase("N/A")){
            return null;
        }

        int bedsAvailable;

        try{ bedsAvailable = Integer.parseInt(rawBedsAvailable);

        }catch(NumberFormatException exception){
            throw new IllegalArgumentException("ERROR: Expected numeric entry");
        }

        if(bedsAvailable < 0){
            throw new IllegalArgumentException("ERROR: Expected positive number");
        }
        return bedsAvailable;
    }
}
