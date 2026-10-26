package co.wethinkcode.healthsafe;

import java.util.*;
import org.junit.jupiter.api.Test;

import javax.print.attribute.standard.Severity;
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
    void shouldLetPublishFailuresReachTheCaller() {
        AlertPublisher broken = alert -> {
            throw new AlertPublisher.AlertPublishException("broker down", new RuntimeException());
        };

        assertThrows(AlertPublisher.AlertPublishException.class,
                () -> new EquipmentAlertService(broken).submit(ALERT));
    }
}
