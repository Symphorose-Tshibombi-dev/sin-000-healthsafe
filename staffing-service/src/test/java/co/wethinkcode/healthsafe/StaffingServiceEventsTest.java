package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.*;

import java.util.*;

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
}
