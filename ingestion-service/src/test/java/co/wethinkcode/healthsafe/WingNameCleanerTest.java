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
}
