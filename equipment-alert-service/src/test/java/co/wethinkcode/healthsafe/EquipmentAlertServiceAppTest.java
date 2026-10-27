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


}
