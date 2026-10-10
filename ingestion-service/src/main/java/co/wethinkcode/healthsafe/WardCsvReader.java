package co.wethinkcode.healthsafe;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

    public static List<String[]> readRecords(String filename) throws Exception{
        List<String[]> records = new ArrayList<>();

        InputStream stream = WardCsvReader.class
                .getClassLoader()
                .getResourceAsStream(filename);
        if(stream == null){
            throw new IllegalArgumentException("CSV file not found" + filename);

        }
        try(CSVReader reader = new CSVReader(new InputStreamReader(stream))){
            reader.readNext();

            String[] row;

            while((row = reader.readNext())!= null){
                records.add(row);
            }
        }
        return records;
    }

}
