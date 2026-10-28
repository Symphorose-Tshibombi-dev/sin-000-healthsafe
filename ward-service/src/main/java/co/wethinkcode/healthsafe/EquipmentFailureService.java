package co.wethinkcode.healthsafe;

import java.util.NoSuchElementException;

/** Checks a reported failure, then sends it to equipment-alert-service through the queue. */
public class EquipmentFailureService {

    public static final int MAX_DESCRIPTION_LENGTH = 500;

    private final WardService wards;
    private final EquipmentFailurePublisher publisher;

    public EquipmentFailureService(WardService wards, EquipmentFailurePublisher publisher) {
        this.wards = wards;
        this.publisher = publisher;
    }

    /**
     * @throws NoSuchElementException     if the ward does not exist
     * @throws IllegalArgumentException   if the report is incomplete or invalid
     * @throws EquipmentFailurePublisher.EquipmentFailurePublishException if the broker is unreachable
     */
    public EquipmentAlertMessage report(String wardId, EquipmentFailureReport report) {
        if (wards.findById(wardId).isEmpty()) {
            throw new NoSuchElementException("Ward " + wardId + " not found");
        }
        if (report == null) {
            throw new IllegalArgumentException("Body is required");
        }
        requireText(report.equipmentId(), "equipmentId");
        requireText(report.description(), "description");
        if (report.description().length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("description must be at most " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        if (report.severity() == null) {
            throw new IllegalArgumentException("severity is required (LOW, MEDIUM, HIGH or CRITICAL)");
        }

        EquipmentAlertMessage message = new EquipmentAlertMessage(report.equipmentId(), wardId, report.description(), report.severity());
        publisher.publish(message);
        return message;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}