package co.wethinkcode.healthsafe;

public class EquipmentAlertValidator {

    public static final int MAX_DESCRIPTION_LENGTH = 500;

    private EquipmentAlertValidator() {
    }

    public static void validate(EquipmentAlert alert) {
        if( alert == null){
            throw new IllegalArgumentException("Alert is required");
        }
    }
}