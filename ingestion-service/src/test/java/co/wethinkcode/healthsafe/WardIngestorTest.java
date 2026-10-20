package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WardIngestorTest {

    // List.of(oneArray) would make a list of Strings, so we use this helper instead
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
    void shouldFlagNonNumericBedsInsteadOfRejecting() {
        IngestionResult result = WardIngestor.ingest(
                rows(new String[]{"W-05", "East Wing", "Paediatrics", "five"}));

        Ward ward = result.wards().get(0);
        assertNull(ward.bedsAvailable());
        assertTrue(ward.notes().contains("five"));
    }

    @Test
    void shouldSkipRowWithMissingWardId() {
        IngestionResult result = WardIngestor.ingest(
                rows(new String[]{" ", "East Wing", "Oncology", "4"}));

        assertTrue(result.wards().isEmpty());
        assertEquals(1, result.issues().size());
        assertTrue(result.issues().get(0).startsWith("Line 2"));
    }

    @Test
    void shouldSkipRowWithWrongColumnCount() {
        IngestionResult result = WardIngestor.ingest(
                rows(new String[]{"W-01", "East Wing"}));

        assertTrue(result.wards().isEmpty());
        assertTrue(result.issues().get(0).contains("expected 4 columns"));
    }

    @Test
    void shouldFlagMissingWing() {
        IngestionResult result = WardIngestor.ingest(
                rows(new String[]{"W-08", "", "Oncology", "4"}));

        Ward ward = result.wards().get(0);
        assertNull(ward.wing());
        assertEquals("wing missing", ward.notes());
    }

    @Test
    void shouldKeepFirstOfTwoCleanDuplicates() {
        IngestionResult result = WardIngestor.ingest(rows(
                new String[]{"W-05", "East Wing", "Paediatrics", "5"},
                new String[]{"w-05", "East Wing", "Paediatrics", "6"}));

        assertEquals(1, result.wards().size());
        assertEquals(5, result.wards().get(0).bedsAvailable());
        assertTrue(result.issues().get(0).startsWith("Line 3"));
    }

    @Test
    void shouldPreferCleanDuplicateOverFlaggedOne() {
        IngestionResult result = WardIngestor.ingest(rows(
                new String[]{"W-05", "East Wing", "Paediatrics", "five"},
                new String[]{"w-05", "East Wing", "Paediatrics", "5"}));

        assertEquals(1, result.wards().size());
        assertEquals(5, result.wards().get(0).bedsAvailable());
        assertTrue(result.issues().get(0).startsWith("Line 2"));
    }

    @Test
    void shouldIngestTheRealCsv() throws Exception {
        List<String[]> rawRows = WardCsvReader.readRecords("wards-outdated.csv");

        IngestionResult result = WardIngestor.ingest(rawRows);

        assertEquals(17, result.wards().size());
        assertEquals(1, result.issues().size());
    }
}