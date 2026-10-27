package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import javax.print.attribute.standard.Severity;
import java.net.URI;
import java.net.http.*;
import java.util.*;

import static io.javalin.apibuilder.ApiBuilder.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private static HttpResponse<String> get(Javalin app, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void shouldAccept202AndQueueAValidAlert() throws Exception {
        FakePublisher publisher = new FakePublisher();
        Javalin app = EquipmentAlertServiceApp.createApp(new EquipmentAlertService(publisher)).start(0);
        try {
            HttpResponse<String> response = post(app, "/equipment-alerts", VALID_BODY);

            assertEquals(202, response.statusCode());
            assertTrue(response.body().contains("QUEUED"));
            assertEquals(List.of(new EquipmentAlert("VENT-12", "W-05", "Lost power", Severity.CRITICAL)),
                    publisher.published);
        } finally {
            app.stop();
        }
    }

    @Test
    void shouldAccept202AndQueueAValidAlert() throws Exception {
        FakePublisher publisher = new FakePublisher();
        Javalin app = EquipmentAlertServiceApp.createApp(new EquipmentAlertService(publisher)).start(0);
        try {
            HttpResponse<String> response = post(app, "/equipment-alerts", VALID_BODY);

            assertEquals(202, response.statusCode());
            assertTrue(response.body().contains("QUEUED"));
            assertEquals(List.of(new EquipmentAlert("VENT-12", "W-05", "Lost power", Severity.CRITICAL)),
                    publisher.published);
        } finally {
            app.stop();
        }
    }
    @Test
    void shouldAnswer400AndNotQueueWhenAFieldIsMissing() throws Exception {
        FakePublisher publisher = new FakePublisher();
        Javalin app = EquipmentAlertServiceApp.createApp(new EquipmentAlertService(publisher)).start(0);
        try {
            HttpResponse<String> response = post(app, "/equipment-alerts",
                    "{\"equipmentId\":\"VENT-12\",\"description\":\"Lost power\",\"severity\":\"HIGH\"}");

            assertEquals(400, response.statusCode());
            assertTrue(response.body().contains("wardId"));
            assertTrue(publisher.published.isEmpty());
        } finally {
            app.stop();
        }
    }
    @Test
    void shouldAnswer503WhenTheBrokerIsUnavailable() throws Exception {
        AlertPublisher broken = alert -> {
            throw new AlertPublisher.AlertPublishException("broker down", new RuntimeException());
        };
        Javalin app = EquipmentAlertServiceApp.createApp(new EquipmentAlertService(broken)).start(0);
        try {
            HttpResponse<String> response = post(app, "/equipment-alerts", VALID_BODY);

            assertEquals(503, response.statusCode());
            assertTrue(response.body().contains("NOT queued"));
        } finally {
            app.stop();
        }
    }


    @Test
    void shouldListProcessedAlerts() throws Exception {
        EquipmentAlertService service = new EquipmentAlertService(new FakePublisher());
        Javalin app = EquipmentAlertServiceApp.createApp(service).start(0);
        try {
            assertEquals("[]", get(app, "/equipment-alerts").body());

            service.recordProcessed(new EquipmentAlert("VENT-12", "W-05", "Lost power", Severity.CRITICAL));

            HttpResponse<String> response = get(app, "/equipment-alerts");
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("VENT-12"));
            assertTrue(response.body().contains("processedAt"));
        } finally {
            app.stop();
        }
    }

}
