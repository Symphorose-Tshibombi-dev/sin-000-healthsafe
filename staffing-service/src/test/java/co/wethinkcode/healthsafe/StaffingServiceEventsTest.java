package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaffingServiceEventsTest {
    private static class FakePublisher implements StaffingEventPublisher {
        final List<Schedule> published = new ArrayList<>();

        @Override
        public void publish(Schedule schedule) {
            published.add(schedule);
        }
    }

    // Handles: the first schedule computed for a ward is broadcast, and equals what the caller got.
    @Test
    void shouldBroadcastTheFirstSchedule() {
        FakePublisher publisher = new FakePublisher();
        StaffingService service = new StaffingService(id -> new WardInfo(id, "East Wing", "Cardiology"), () -> 3, publisher);

        Schedule schedule = service.scheduleFor("W-01");

        assertEquals(List.of(schedule), publisher.published);
    }
    // Handles: reading the same schedule again does not broadcast again.
    @Test
    void shouldNotBroadcastTheSameScheduleTwice() {
        FakePublisher publisher = new FakePublisher();
        StaffingService service = new StaffingService(id -> new WardInfo(id, "East Wing", "Cardiology"), () -> 3, publisher);

        service.scheduleFor("W-01");
        service.scheduleFor("W-01");

        assertEquals(1, publisher.published.size());
    }

    // Handles: when the alert level changes the schedule changes, so it is broadcast again.
    @Test
    void shouldBroadcastAgainWhenTheAlertLevelChanges() {
        FakePublisher publisher = new FakePublisher();
        AtomicInteger level = new AtomicInteger(1);
        StaffingService service = new StaffingService(id -> new WardInfo(id, "East Wing", "Cardiology"), level::get, publisher);

        service.scheduleFor("W-01");
        level.set(8);
        service.scheduleFor("W-01");

        assertEquals(2, publisher.published.size());
        assertEquals("CODE BLUE", publisher.published.get(1).mode());
    }
}
