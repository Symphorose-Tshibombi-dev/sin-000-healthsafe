package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


public class WardCsvReaderTest {

    @Test
    void shouldReadCsvHeader() {

        String[] expectedHeaders = {"wing_name","beds_available"};
        String[] result = WardCsvReader.readHeaders("sample-wards.csv");

        assertArrayEquals(expectedHeaders , result);
    }
}
