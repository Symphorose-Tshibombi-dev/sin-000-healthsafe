package co.wethinkcode.healthsafe.exceptions;

import co.wethinkcode.healthsafe.Schedule;

public interface SchedulePublisher {

    void publish(Schedule schedule);
}