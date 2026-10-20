package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class WardCsvReaderTest {

    @Test
    void shouldReadCsvHeader() throws Exception {

        String[] expectedHeaders = {"wing_name","beds_available"};
        String[] result = WardCsvReader.readHeaders("sample-wards.csv");

        assertArrayEquals(expectedHeaders , result);
    }

    @Test
    void shouldReadAllWardRecords() throws Exception {

        List<String[]> records = WardCsvReader.readRecords("sample-wards.csv");

        assertEquals(3, records.size());
        assertArrayEquals(new String[]{"east wing", "12"}, records.get(0));
        assertArrayEquals(new String[]{"NORTH WING", "5"}, records.get(1));
        assertArrayEquals(new String[]{"south wing", "N/A"}, records.get(2));
    }
}
