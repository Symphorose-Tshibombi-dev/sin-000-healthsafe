package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class BedAvailabilityCleanerTest {

    @Test

    void convertValidBedNumberCsvToInteger() {
        String rawBedsAvailable = "3";
        int result = BedAvailabilityCleaner.clean(rawBedsAvailable);
        assertEquals(3, result);
    }
}
