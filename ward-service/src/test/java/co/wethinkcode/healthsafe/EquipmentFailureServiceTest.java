package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests: EquipmentFailureService.report(): ward check, validation, then publish.
 * A fake publisher replaces the broker.
 */
public class EquipmentFailureServiceTest {

    private static class FakePublisher implements EquipmentFailurePublisher {
        final List<EquipmentAlertMessage> published = new ArrayList<>();

        @Override
        public void publish(EquipmentAlertMessage message) {
            published.add(message);
        }
    }

    private static final WardService WARDS = new WardService(List.of(
            new Ward("W-05", "East Wing", "ICU", 2, null)));

    private static EquipmentFailureReport report() {
        return new EquipmentFailureReport("VENT-12", "Lost power", Severity.CRITICAL);
    }

    private static String errorFor(EquipmentFailureService service, EquipmentFailureReport report) {
        return assertThrows(IllegalArgumentException.class, () -> service.report("W-05", report)).getMessage();
    }

    // Handles: success path. The queue message carries the wardId from the URL, not from the body.
    @Test
    void shouldPublishAValidFailureWithTheWardFromTheUrl() {
        FakePublisher publisher = new FakePublisher();

        new EquipmentFailureService(WARDS, publisher).report("W-05", report());

        assertEquals(List.of(new EquipmentAlertMessage("VENT-12", "W-05", "Lost power", Severity.CRITICAL)),
                publisher.published);
    }

    // Handles: the ward check. An unknown ward throws NoSuchElementException (404) and nothing is queued.
    @Test
    void shouldRejectAnUnknownWard() {
        FakePublisher publisher = new FakePublisher();
        EquipmentFailureService service = new EquipmentFailureService(WARDS, publisher);

        NoSuchElementException error = assertThrows(NoSuchElementException.class, () -> service.report("W-99", report()));

        assertTrue(error.getMessage().contains("W-99"));
        assertTrue(publisher.published.isEmpty());
    }

    // Handles: requireText() for equipmentId and description, and the severity null check.
    @Test
    void shouldRejectMissingFieldsAndNameThem() {
        FakePublisher publisher = new FakePublisher();
        EquipmentFailureService service = new EquipmentFailureService(WARDS, publisher);

        assertTrue(errorFor(service, new EquipmentFailureReport(null, "x", Severity.LOW)).contains("equipmentId"));
        assertTrue(errorFor(service, new EquipmentFailureReport("  ", "x", Severity.LOW)).contains("equipmentId"));
        assertTrue(errorFor(service, new EquipmentFailureReport("V", null, Severity.LOW)).contains("description"));
        assertTrue(errorFor(service, new EquipmentFailureReport("V", "x", null)).contains("severity"));
        assertTrue(publisher.published.isEmpty());
    }

    // Handles: the MAX_DESCRIPTION_LENGTH check and the null body check.
    @Test
    void shouldRejectATooLongDescriptionAndANullBody() {
        EquipmentFailureService service = new EquipmentFailureService(WARDS, new FakePublisher());
        String tooLong = "x".repeat(EquipmentFailureService.MAX_DESCRIPTION_LENGTH + 1);

        assertTrue(errorFor(service, new EquipmentFailureReport("V", tooLong, Severity.LOW)).contains("at most"));
        assertTrue(errorFor(service, null).contains("required"));
    }

    // Handles: a publish failure (broker down) reaches the caller, so the route can answer 503.
    @Test
    void shouldLetPublishFailuresReachTheCaller() {
        EquipmentFailurePublisher broken = message -> {
            throw new EquipmentFailurePublisher.EquipmentFailurePublishException("broker down", new RuntimeException());
        };

        assertThrows(EquipmentFailurePublisher.EquipmentFailurePublishException.class,
                () -> new EquipmentFailureService(WARDS, broken).report("W-05", report()));
    }
}