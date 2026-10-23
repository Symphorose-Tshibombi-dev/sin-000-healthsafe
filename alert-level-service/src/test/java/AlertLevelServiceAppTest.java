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




}