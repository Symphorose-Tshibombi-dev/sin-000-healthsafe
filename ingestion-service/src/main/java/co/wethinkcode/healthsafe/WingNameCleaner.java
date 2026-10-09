package co.wethinkcode.healthsafe;

public class WingNameCleaner {

    public static String clean(String rawWingName){
        if(rawWingName.equals("east wing")){
            rawWingName = rawWingName.substring(0,1).toUpperCase() +
                    rawWingName.substring(1,5).toLowerCase()+
                    rawWingName.substring(5,6).toUpperCase() +
                    rawWingName.substring(6).toLowerCase();

        }

        return rawWingName;
    }
}
