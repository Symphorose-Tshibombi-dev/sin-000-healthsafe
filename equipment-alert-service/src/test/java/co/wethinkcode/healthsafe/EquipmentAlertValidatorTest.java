package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EquipmentAlertValidatorTest {

    private static EquipmentAlert valid() {
        return new EquipmentAlert("VENT-12", "W-05", "Ventilator lost power", Severity.CRITICAL);
    }

    private static String errorFor(EquipmentAlert alert) {
        return assertThrows(IllegalArgumentException.class, () -> EquipmentAlertValidator.validate(alert)).getMessage();
    }

    @Test
    void shouldAcceptAValidAlert() {
        assertDoesNotThrow(() -> EquipmentAlertValidator.validate(valid()));
    }

    @Test
    void shouldRejectMissingOrBlankEquipmentId() {
        assertTrue(errorFor(new EquipmentAlert(null, "W-05", "Broken", Severity.LOW)).contains("equipmentId"));
        assertTrue(errorFor(new EquipmentAlert("  ", "W-05", "Broken", Severity.LOW)).contains("equipmentId"));
    }

    @Test
    void shouldRejectMissingWardId() {
        assertTrue(errorFor(new EquipmentAlert("VENT-12", "", "Broken", Severity.LOW)).contains("wardId"));
    }

    @Test
    void shouldRejectMissingDescription() {
        assertTrue(errorFor(new EquipmentAlert("VENT-12", "W-05", null, Severity.LOW)).contains("description"));
    }

    @Test
    void shouldRejectTooLongDescription() {
        String tooLong = "x".repeat(EquipmentAlertValidator.MAX_DESCRIPTION_LENGTH + 1);
        assertTrue(errorFor(new EquipmentAlert("VENT-12", "W-05", tooLong, Severity.LOW)).contains("at most"));
    }

    @Test
    void shouldRejectMissingSeverity() {
        assertTrue(errorFor(new EquipmentAlert("VENT-12", "W-05", "Broken", null)).contains("severity"));
    }

    @Test
    void shouldRejectANullAlert() {
        assertTrue(errorFor(null).contains("required"));
    }
}