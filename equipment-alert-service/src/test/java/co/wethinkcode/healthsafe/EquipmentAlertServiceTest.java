package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EquipmentAlertServiceTest {

    private static final EquipmentAlert ALERT = new EquipmentAlert("VENT-12", "W-05", "Lost power", Severity.CRITICAL);

    private static class FakePublisher implements AlertPublisher {
        final List<EquipmentAlert> published = new ArrayList<>();

        @Override
        public void publish(EquipmentAlert alert) {
            published.add(alert);
        }
    }

    @Test
    void shouldPublishAValidAlert() {
        FakePublisher publisher = new FakePublisher();

        new EquipmentAlertService(publisher).submit(ALERT);

        assertEquals(List.of(ALERT), publisher.published);
    }

    @Test
    void shouldNotPublishAnInvalidAlert() {
        FakePublisher publisher = new FakePublisher();
        EquipmentAlertService service = new EquipmentAlertService(publisher);

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(new EquipmentAlert("VENT-12", "W-05", "Lost power", null)));

        assertTrue(publisher.published.isEmpty());
    }

    @Test
    void shouldLetPublishFailuresReachTheCaller() {
        AlertPublisher broken = alert -> {
            throw new AlertPublisher.AlertPublishException("broker down", new RuntimeException());
        };

        assertThrows(AlertPublisher.AlertPublishException.class,
                () -> new EquipmentAlertService(broken).submit(ALERT));
    }

    @Test
    void shouldListProcessedAlerts() {
        EquipmentAlertService service = new EquipmentAlertService(new FakePublisher());
        assertTrue(service.processedAlerts().isEmpty());

        service.recordProcessed(ALERT);

        assertEquals(1, service.processedAlerts().size());
        assertEquals(ALERT, service.processedAlerts().get(0).alert());
        assertNotNull(service.processedAlerts().get(0).processedAt());
    }
}