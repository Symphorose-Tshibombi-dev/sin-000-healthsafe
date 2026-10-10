package co.wethinkcode.healthsafe;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;

public class WardCsvReader {

    public static String[] readHeaders(String filename) throws Exception{
        InputStream stream = WardCsvReader.class
                .getClassLoader()
                .getResourceAsStream(filename);
        try (CSVReader reader = new CSVReader(new InputStreamReader(stream))) {
            String[] row = reader.readNext();
            return row;
        }


    }

}
