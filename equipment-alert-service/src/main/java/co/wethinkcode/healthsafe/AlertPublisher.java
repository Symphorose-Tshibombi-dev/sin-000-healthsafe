package co.wethinkcode.healthsafe;

/** Sends an alert towards the queue. An interface so tests can swap in a fake. */
public interface AlertPublisher {

    void publish(EquipmentAlert alert);

    /** The alert could not be handed to the broker (broker down, serialisation failure...). */
    class AlertPublishException extends RuntimeException {
        public AlertPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}