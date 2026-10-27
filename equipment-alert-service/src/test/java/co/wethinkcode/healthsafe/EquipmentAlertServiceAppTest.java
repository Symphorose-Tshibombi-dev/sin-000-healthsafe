package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import java.net.URI;
import java.net.http.*;
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

    private static HttpResponse<String> get(Javalin app, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }


}
