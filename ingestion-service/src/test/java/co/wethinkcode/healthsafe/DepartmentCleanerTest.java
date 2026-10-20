package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DepartmentCleanerTest {
    @Test

    void shouldCapitaliseDepartment() {
        String rawDepartmentName = "PAEDIATRICS";

        String result = DepartmentCleaner.clean(rawDepartmentName);
        assertEquals("Paediatrics" , result);
    }

    @Test
    void shouldSpellCorrectly(){
        String rawDepartmentName = "Pediatrics";

        String result = DepartmentCleaner.clean(rawDepartmentName);
        assertEquals("Paediatrics", result);
    }

    @Test

    void shouldCapitaliseICU(){
        String rawDepartmentName = "ICU";

        String result = DepartmentCleaner.clean(rawDepartmentName);
        assertEquals("ICU", result);
    }



    @Test

    void shouldTitleCaseOtherDepartments(){
        String rawDepartmentName = "cardiology";

        String result = DepartmentCleaner.clean(rawDepartmentName);
        assertEquals("Cardiology", result);
    }

    @Test
    void shouldTrimPadding(){
        String rawDepartmentName = " cardiology ";

        String result = DepartmentCleaner.clean(rawDepartmentName);
        assertEquals("Cardiology",result);
    }

    @Test
    void shouldReturnNullForMissingOrPlaceholder() {
        assertNull(DepartmentCleaner.clean(null));
        assertNull(DepartmentCleaner.clean("   "));
        assertNull(DepartmentCleaner.clean("TBD"));
    }
}
