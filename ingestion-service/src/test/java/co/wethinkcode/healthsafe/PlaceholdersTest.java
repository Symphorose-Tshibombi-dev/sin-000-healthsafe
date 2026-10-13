package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlaceholdersTest {

    @Test
    void shouldRecognisePlaceholdersIgnoringCaseAndPadding() {
        assertTrue(Placeholders.isPlaceholder("TBD"));
        assertTrue(Placeholders.isPlaceholder(" n/a "));
        assertTrue(Placeholders.isPlaceholder("Unknown"));
        assertTrue(Placeholders.isPlaceholder("-"));
        assertTrue(Placeholders.isPlaceholder("NaN"));
    }

    @Test
    void shouldTreatNullAndBlankAsPlaceholders() {
        assertTrue(Placeholders.isPlaceholder(null));
        assertTrue(Placeholders.isPlaceholder("   "));
    }

    @Test
    void shouldNotTreatRealValuesAsPlaceholders() {
        assertFalse(Placeholders.isPlaceholder("5"));
        assertFalse(Placeholders.isPlaceholder("-1"));
        assertFalse(Placeholders.isPlaceholder("five"));
    }
}