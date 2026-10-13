package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WardIdCleanerTest {

    @Test

    void shouldUpperCaseId(){
        String rawWardId = "w-05";

        String result = WardIdCleaner.clean(rawWardId);
        assertEquals("W-05" ,result);
    }
}
