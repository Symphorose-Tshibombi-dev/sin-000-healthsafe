package co.wethinkcode.healthsafe;

public class WingNameCleaner {

    public static String clean(String rawWingName){
        String[] words = rawWingName.toLowerCase().trim().split("\\s+");


        for (int i = 0; i < words.length; i++){
            words[i] =
                    words[i].substring(0, 1).toUpperCase() +
                    words[i].substring(1).toLowerCase();
        }

        return String.join(" ",words);
    }
}
