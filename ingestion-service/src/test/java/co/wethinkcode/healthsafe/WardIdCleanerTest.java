package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WardIdCleanerTest {

    @Test

    void shouldUpperCaseId(){
        String rawWardId = "w-05";

        String result = WardIdCleaner.clean(rawWardId);
        assertEquals("W-05" ,result);
    }

    @Test

    void shouldTrimPadding(){
        String rawWardId = " W-03 ";

        String result = WardIdCleaner.clean(rawWardId);
        assertEquals("W-03",result);
    }

    @Test
    void shouldRejectMissingId() {
        String rawWardId = " ";



        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> WardIdCleaner.clean(rawWardId)
        );



        assertEquals("ERROR: Expected ward ID entry.", exception.getMessage());


    }

    @Test
    void shouldRejectNull(){
        String rawWardId = null;

        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                ()-> WardIdCleaner.clean(rawWardId)

        );
            assertEquals("ERROR: Expected ward ID entry." ,exception2.getMessage());
        }
    @Test
    void shouldRejectPlaceHolder(){
        String rawWardId = "TBD";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> WardIdCleaner.clean(rawWardId)
        );
        assertEquals("ERROR: Expected ward ID entry.", exception.getMessage());
    }

    @Test
    void shouldRejectBadFormat() {
        String rawWardId = "ward5";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> WardIdCleaner.clean(rawWardId)
        );

        assertEquals("ERROR: Invalid ward ID format.", exception.getMessage());
    }
}
