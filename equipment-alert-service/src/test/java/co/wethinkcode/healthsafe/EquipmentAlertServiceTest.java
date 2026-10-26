package co.wethinkcode.healthsafe;

import javax.print.attribute.standard.Severity;

public class EquipmentAlertServiceTest {


    private static final EquipmentAlert ALERT = new EquipmentAlert("VENT-12", "W-05", "Lost power", Severity.CRITICAL);

    private static class FakePublisher implements AlertPublisher {
        final List<EquipmentAlert> published = new ArrayList<>();

        @Override
        public void publish(EquipmentAlert alert) {
            published.add(alert);
        }
    }
}
