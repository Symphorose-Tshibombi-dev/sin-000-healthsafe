package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class AlertLevelServiceAppTest {

    private static HttpResponse<String> get(Javalin app, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> put(Javalin app, String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void shouldAnswerOkOnHealth() throws Exception {
        Javalin app = AlertLevelServiceApp.createApp(new AlertLevelService()).start(0);
        try {
            HttpResponse<String> response = get(app, "/health");

            assertEquals(200, response.statusCode());
            assertEquals("OK", response.body());
        } finally {
            app.stop();
        }
    }

    @Test
    void shouldReturnTheCurrentLevel() throws Exception {
        Javalin app = AlertLevelServiceApp.createApp(new AlertLevelService()).start(0);
        try {
            HttpResponse<String> response = get(app, "/alert-level");

            assertEquals(200, response.statusCode());
            assertEquals("{\"level\":0}", response.body());
        } finally {
            app.stop();
        }
    }

    @Test
    void shouldChangeTheLevelAndReturnItLater() throws Exception {
        Javalin app = AlertLevelServiceApp.createApp(new AlertLevelService()).start(0);
        try {
            HttpResponse<String> changed = put(app, "/alert-level", "{\"level\":5}");
            assertEquals(200, changed.statusCode());

            HttpResponse<String> response = get(app, "/alert-level");
            assertEquals("{\"level\":5}", response.body());
        } finally {
            app.stop();
        }
    }

    @Test
    void shouldAnswer400WhenLevelIsOutOfRange() throws Exception {
        Javalin app = AlertLevelServiceApp.createApp(new AlertLevelService()).start(0);
        try {
            HttpResponse<String> response = put(app, "/alert-level", "{\"level\":9}");

            assertEquals(400, response.statusCode());
            assertTrue(response.body().contains("between 0 and 8"));
            assertEquals("{\"level\":0}", get(app, "/alert-level").body());
        } finally {
            app.stop();
        }
    }

    @Test
    void shouldAnswer400WhenBodyIsNotAValidLevel() throws Exception {
        Javalin app = AlertLevelServiceApp.createApp(new AlertLevelService()).start(0);
        try {
            assertEquals(400, put(app, "/alert-level", "not json").statusCode());
            assertEquals(400, put(app, "/alert-level", "{\"level\":\"high\"}").statusCode());
            assertEquals(400, put(app, "/alert-level", "{}").statusCode());
        } finally {
            app.stop();
        }
    }


}