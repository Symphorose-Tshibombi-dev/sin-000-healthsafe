package co.wethinkcode.healthsafe;

/** Puts an equipment failure on the queue. An interface so tests can fake the broker. */
public interface EquipmentFailurePublisher {

    void publish(EquipmentAlertMessage message);

    /** The failure could not be handed to the broker (broker down, serialisation failure...). */
    class EquipmentFailurePublishException extends RuntimeException {
        public EquipmentFailurePublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}