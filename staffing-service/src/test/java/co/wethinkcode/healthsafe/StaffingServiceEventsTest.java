package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.exceptions.StaffingEventPublisher;
import co.wethinkcode.healthsafe.exceptions.WardNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests: StaffingService.scheduleFor() and its private broadcastIfChanged():
 * when a schedule is announced on the topic. A fake publisher replaces the broker.
 */
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

    // Handles: each ward is tracked separately, so two wards both get their own first event.
    @Test
    void shouldBroadcastOncePerWard() {
        FakePublisher publisher = new FakePublisher();
        StaffingService service = new StaffingService(id -> new WardInfo(id, "East Wing", "Cardiology"), () -> 3, publisher);

        service.scheduleFor("W-01");
        service.scheduleFor("W-02");

        assertEquals(2, publisher.published.size());
    }

    // Handles: broadcast is best effort. A broker failure must not break the REST answer,
    // and the same schedule is retried on the next read.
    @Test
    void shouldStillReturnTheScheduleWhenTheBrokerIsDownAndRetryLater() {
        AtomicInteger attempts = new AtomicInteger();
        StaffingEventPublisher flaky = schedule -> {
            if (attempts.incrementAndGet() == 1) {
                throw new StaffingEventPublisher.StaffingEventPublishException("broker down", new RuntimeException());
            }
        };
        StaffingService service = new StaffingService(id -> new WardInfo(id, "East Wing", "Cardiology"), () -> 3, flaky);

        assertDoesNotThrow(() -> service.scheduleFor("W-01"));
        assertDoesNotThrow(() -> service.scheduleFor("W-01"));

        assertEquals(2, attempts.get());
    }

    // Handles: an unknown ward throws before anything is broadcast.
    @Test
    void shouldNotBroadcastForAnUnknownWard() {
        FakePublisher publisher = new FakePublisher();
        StaffingService service = new StaffingService(id -> {
            throw new WardNotFoundException(id);
        }, () -> 3, publisher);

        assertThrows(WardNotFoundException.class, () -> service.scheduleFor("W-99"));

        assertTrue(publisher.published.isEmpty());
    }
}