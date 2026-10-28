package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import java.net.URI;
import java.net.http.*;
import java.util.*;

public class AlertLevelTopicTest {

    private static class FakePublisher implements AlertLevelPublisher {
        final List<AlertLevelChange> published = new ArrayList<>();

        @Override
        public void publish(AlertLevelChange change) {
            published.add(change);
        }
    }

    private static HttpResponse<String> put(Javalin app, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + "/alert-level"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
