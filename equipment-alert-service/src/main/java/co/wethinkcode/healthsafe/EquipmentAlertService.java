package co.wethinkcode.healthsafe;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Producer side: validate, then hand the alert to the queue.
 * Consumer side: remember what has been processed (in memory, resets on restart).
 */
public class EquipmentAlertService {

    private final AlertPublisher publisher;
    private final List<ProcessedAlert> processed = new CopyOnWriteArrayList<>();

    public EquipmentAlertService(AlertPublisher publisher) {
        this.publisher = publisher;
    }

    /** @throws IllegalArgumentException if invalid, AlertPublisher.AlertPublishException if the broker is unreachable */
    public void submit(EquipmentAlert alert) {
        EquipmentAlertValidator.validate(alert);
        publisher.publish(alert);
    }

    public void recordProcessed(EquipmentAlert alert) {
        processed.add(new ProcessedAlert(alert, Instant.now().toString()));
    }

    public List<ProcessedAlert> processedAlerts() {
        return List.copyOf(processed);
    }
}