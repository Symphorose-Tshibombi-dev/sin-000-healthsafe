package co.wethinkcode.healthsafe.exceptions;

import co.wethinkcode.healthsafe.Schedule;

/** Broadcasts a changed schedule. An interface so tests can fake the broker. */
public interface StaffingEventPublisher {

    void publish(Schedule schedule);

    /** The event could not be handed to the broker (broker down, serialisation failure...). */
    class StaffingEventPublishException extends RuntimeException {
        public StaffingEventPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}