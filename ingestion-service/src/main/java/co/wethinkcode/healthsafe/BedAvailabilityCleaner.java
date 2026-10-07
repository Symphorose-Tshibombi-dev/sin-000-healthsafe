package co.wethinkcode.healthsafe;

public class BedAvailabilityCleaner {

    public static int clean(String rawBedsAvailable){

        int bedsAvailable = Integer.parseInt(rawBedsAvailable);

        if(bedsAvailable < 0){
            throw new IllegalArgumentException("ERROR: Expected positive number");
        }
        return bedsAvailable;
    }
}
