package co.wethinkcode.healthsafe;

import java.util.*;

public class EquipmentAlertServiceAppTest {
    private static final String VALID_BODY =
            "{\"equipmentId\":\"VENT-12\",\"wardId\":\"W-05\",\"description\":\"Lost power\",\"severity\":\"CRITICAL\"}";

    private static class FakePublisher implements AlertPublisher {
        final List<EquipmentAlert> published = new ArrayList<>();

        @Override
        public void publish(EquipmentAlert alert) {
            published.add(alert);
        }
    }

}
