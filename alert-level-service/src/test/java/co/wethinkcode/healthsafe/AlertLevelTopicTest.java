package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void shouldPublishAValidChange() throws Exception {
        FakePublisher publisher = new FakePublisher();
        Javalin app = AlertLevelServiceApp.createApp(new AlertLevelService(), publisher).start(0);
        try {
            assertEquals(200, put(app, "{\"level\":5}").statusCode());

            assertEquals(1, publisher.published.size());
            assertEquals(0, publisher.published.get(0).previousLevel());
            assertEquals(5, publisher.published.get(0).level());
            assertNotNull(publisher.published.get(0).changedAt());
        } finally {
            app.stop();
        }
    }
}
