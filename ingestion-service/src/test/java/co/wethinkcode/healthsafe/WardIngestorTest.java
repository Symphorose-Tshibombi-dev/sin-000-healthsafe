package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WardIngestorTest {

    private static List<String[]> rows(String[]... rows) {
        return Arrays.asList(rows);
    }

    @Test
    void shouldCleanAllFieldsOfAGoodRow() {
        IngestionResult result = WardIngestor.ingest(
                rows(new String[]{"w-01", " east wing ", "PEDIATRICS", "3"}));

        assertEquals(new Ward("W-01", "East Wing", "Paediatrics", 3, null), result.wards().get(0));
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void shouldFlagNonNumericBedsInsteadOfRejecting(){

        IngestionResult result = WardIngestor.ingest(
          row(new String[]{"W-05", "East Wing" , "Paediatrics","5"})
        );

        Ward ward = result.wards().get(0);
        assertNull(ward.bedsAvailable());
        assertTrue(ward.notes().contains("five"));
    }


}
