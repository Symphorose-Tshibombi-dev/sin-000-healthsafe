package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.exceptions.DownstreamUnavailableException;
import co.wethinkcode.healthsafe.exceptions.WardNotFoundException;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class StaffingServiceAppTest {

    private static HttpResponse<String> get(Javalin app, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void shouldAnswerOkOnHealth() throws Exception {
        Javalin app = StaffingServiceApp.createApp(new StaffingService(
                id -> new WardInfo(id, "East Wing", "Cardiology"), () -> 0)).start(0);
        try {
            HttpResponse<String> response = get(app, "/health");

            assertEquals(200, response.statusCode());
            assertEquals("OK", response.body());
        } finally {
            app.stop();
        }
    }

    @Test
    void shouldReturnAScheduleForAKnownWard() throws Exception {
        Javalin app = StaffingServiceApp.createApp(new StaffingService(
                id -> new WardInfo(id, "East Wing", "Cardiology"), () -> 8)).start(0);
        try {
            HttpResponse<String> response = get(app, "/schedule/W-01");

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("\"wardId\":\"W-01\""));
            assertTrue(response.body().contains("\"doctorsOnCall\":4"));
            assertTrue(response.body().contains("CODE BLUE"));
        } finally {
            app.stop();
        }
    }

    @Test
    void shouldAnswer404ForAnUnknownWard() throws Exception {
        Javalin app = StaffingServiceApp.createApp(new StaffingService(
                id -> {
                    throw new WardNotFoundException(id);
                }, () -> 0)).start(0);
        try {
            HttpResponse<String> response = get(app, "/schedule/W-99");

            assertEquals(404, response.statusCode());
            assertTrue(response.body().contains("W-99"));
        } finally {
            app.stop();
        }
    }

    @Test
    void shouldAnswer503WhenADownstreamServiceIsUnavailable() throws Exception {
        Javalin app = StaffingServiceApp.createApp(new StaffingService(
                id -> new WardInfo(id, "East Wing", "Cardiology"),
                () -> {
                    throw new DownstreamUnavailableException("Could not reach alert-level-service");
                })).start(0);
        try {
            HttpResponse<String> response = get(app, "/schedule/W-01");

            assertEquals(503, response.statusCode());
            assertTrue(response.body().contains("alert-level-service"));
        } finally {
            app.stop();
        }
    }
}