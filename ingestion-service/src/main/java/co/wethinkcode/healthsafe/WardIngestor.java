package co.wethinkcode.healthsafe;

import java.util.*;

public class WardIngestor {
    private static final int EXPECTED_COLUMNS = 4;
    private static final int FIRST_DATA_LINE = 2;

    public static IngestionResult ingest(List<String[]> rawRows){
        Map<String, Ward> wardsById = new LinkedHashMap<>();
        Map<String, Integer> keptLine =new HashMap<>();
        List<String> issues = new ArrayList<>();


        for(int i = 0; i < rawRows.size(); i++){
            int line = i + FIRST_DATA_LINE;
            String[] row = rawRows.get(i);

            if (row.length != EXPECTED_COLUMNS){
                issues.add("Line " + line + ": expected " + EXPECTED_COLUMNS
                                + " columns but found " + row.length + "- row skipped");
                continue;
            }


        }
    }
}
