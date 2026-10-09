package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class WingNameCleanerTest {

    @Test

    void shouldCapitaliseWingName(){
        String rawWingName = "east wing";

        String result = WingNameCleaner.clean(rawWingName);
        assertEquals("East Wing" , result);
    }

    @Test

    void shouldCapitaliseNorthWing() {
        String rawWingName = "north wing";

        String result = WingNameCleaner.clean(rawWingName);
        assertEquals("North Wing" , result);
    }

    @Test

    void shouldCapitaliseSouthWing() {
        String rawWingName = "south wing";

        String result = WingNameCleaner.clean(rawWingName);
        assertEquals("South Wing" , result);
    }

    @Test

    void shouldCapitaliseWestWing() {
        String rawWingName = "west wing";

        String result = WingNameCleaner.clean(rawWingName);
        assertEquals("West Wing" , result);
    }

    @Test
    void shouldHandleExtraSpacesInWingName() {
        String rawWingName = " south   wing ";

        String result = WingNameCleaner.clean(rawWingName);
        assertEquals("South Wing" , result);
    }

    @Test

    void shouldHandleMissingWingName(){
        String rawWingName = "";

        String result = WingNameCleaner.clean(rawWingName);
        assertNull(result);
    }

    @Test

    void shouldHandleNullWingName(){
        String rawWingName = null;

        String result = WingNameCleaner.clean(rawWingName);
        assertNull(result);
    }
}
