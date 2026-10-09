package co.wethinkcode.healthsafe;

public class BedAvailabilityCleaner {

    public static Integer clean(String rawBedsAvailable){

        if(rawBedsAvailable == null){
            return null;
        }


        String cleanedValue = rawBedsAvailable.trim();

        if(cleanedValue.equalsIgnoreCase("N/A")|| cleanedValue.isEmpty()){
            return null;
        }

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
