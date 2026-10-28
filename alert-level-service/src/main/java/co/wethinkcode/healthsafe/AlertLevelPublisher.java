package co.wethinkcode.healthsafe;

/** Announces a level change to whoever is listening. An interface so tests can fake the broker. */
public interface AlertLevelPublisher {

    void publish(AlertLevelChange change);

    /** The change could not be handed to the broker (broker down, serialisation failure...). */
    class AlertLevelPublishException extends RuntimeException {
        public AlertLevelPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}