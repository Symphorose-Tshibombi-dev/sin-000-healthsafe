package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepartmentCleanerTest {
    @Test

    void shouldCapitaliseWestWing() {
        String rawDepartmentName = "PAEDIATRICS";

        String result = DepartmentCleaner.clean(rawDepartmentName);
        assertEquals("Paediatrics" , result);
    }

}
