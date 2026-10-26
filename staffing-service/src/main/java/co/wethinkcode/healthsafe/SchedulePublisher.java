package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.Schedule;

public interface SchedulePublisher {

    void publish(Schedule schedule);
}