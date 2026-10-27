package co.wethinkcode.healthsafe;

public class EquipmentAlertValidator {

    public static final int MAX_DESCRIPTION_LENGTH = 500;

    private EquipmentAlertValidator() {
    }

    public static void validate(EquipmentAlert alert) {
        if( alert == null){
            throw new IllegalArgumentException("Alert is required");
        }
        requireText(alert.equipmentId(), "equipmentId");
        requireText(alert.wardId(), "wardId");
        requireText(alert.description(), "description");
        if (alert.description().length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("description must be at most " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        if (alert.severity() == null) {
            throw new IllegalArgumentException("severity is required (LOW, MEDIUM, HIGH or CRITICAL)");
        }
    }
    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}