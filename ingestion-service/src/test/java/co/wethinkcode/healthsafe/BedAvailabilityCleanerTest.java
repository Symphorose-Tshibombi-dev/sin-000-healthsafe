package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class BedAvailabilityCleanerTest {

    @Test

    void convertValidBedNumberCsvToInteger() {
        String rawBedsAvailable = "3";
        Integer result = BedAvailabilityCleaner.clean(rawBedsAvailable);
        assertEquals(3, result);
    }

    @Test

    void convertZeroFromCsvToInteger(){
        String rawBedsAvailable = "0";
        Integer result = BedAvailabilityCleaner.clean(rawBedsAvailable);
        assertEquals(0,result);

    }

    @Test

    void rejectNegativeBedAvailability(){
        String rawBedsAvailable = "-1";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                ()-> BedAvailabilityCleaner.clean(rawBedsAvailable)
        );

        assertEquals("ERROR: Expected positive number",exception.getMessage());

    }

    @Test

    void shouldTreatNullAsUnknown(){
        String rawBedsAvailable = "N/A";
        Integer result = BedAvailabilityCleaner.clean(rawBedsAvailable);

        assertNull(result);
    }

    @Test

    void rejectNonNumericBedAvailability(){
        String rawBedsAvailable = "five";

        IllegalArgumentException exception = assertThrows(
              IllegalArgumentException.class,
                ()-> BedAvailabilityCleaner.clean(rawBedsAvailable)
        );

        assertEquals("ERROR: Expected numeric entry",exception.getMessage());
    }

    @Test

    void handleNAWithExtraSpaces(){
        String rawBedsAvailable = " N/A ";

        Integer result = BedAvailabilityCleaner.clean(rawBedsAvailable);
        assertNull(result);
    }

    @Test

    void handleNumericWithExtraSpace(){

        String rawBedsAvailability = " 12 ";

        Integer result = BedAvailabilityCleaner.clean(rawBedsAvailability);
        assertEquals(12 , result);
    }

    @Test

    void handleEmptyBedAvailability() {
        String rawBedsAvailability = "";

        Integer result = BedAvailabilityCleaner.clean(rawBedsAvailability);
        assertNull(result);
    }

    @Test

    void handleNullBedAvailability() {
        String rawBedsAvailability = null;

        Integer result = BedAvailabilityCleaner.clean(rawBedsAvailability);
        assertNull(result);
    }
}
