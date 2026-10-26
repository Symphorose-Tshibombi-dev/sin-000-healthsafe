package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import javax.print.attribute.standard.Severity;

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



}
