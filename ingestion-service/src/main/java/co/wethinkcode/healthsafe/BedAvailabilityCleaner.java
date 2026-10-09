package co.wethinkcode.healthsafe;

public class BedAvailabilityCleaner {

    public static Integer clean(String rawBedsAvailable){

        if(rawBedsAvailable.equalsIgnoreCase("N/A")){
            return null;
        }

        int bedsAvailable = Integer.parseInt(rawBedsAvailable);

        if(bedsAvailable < 0){
            throw new IllegalArgumentException("ERROR: Expected positive number");
        }
        return bedsAvailable;
    }
}
