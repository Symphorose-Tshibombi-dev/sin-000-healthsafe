package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests: the two MQ-related routes in WardServiceApp.createApp(service, failures, board):
 *   POST /wards/{id}/equipment-failures   (202 / 400 / 404 / 503)   -> queue, stage 4
 *   GET  /wards/{id}/staffing             (200 / 404)               -> topic, stage 3
 * Fakes replace the broker. The existing routes are covered by WardServiceAppTest.
 */
public class WardRoutesMqTest {

    private static final String VALID_BODY = "{\"equipmentId\":\"VENT-12\",\"description\":\"Lost power\",\"severity\":\"CRITICAL\"}";

    private static class FakePublisher implements EquipmentFailurePublisher {
        final List<EquipmentAlertMessage> published = new ArrayList<>();

        @Override
        public void publish(EquipmentAlertMessage message) {
            published.add(message);
        }
    }

    private static WardService wards() {
        return new WardService(List.of(new Ward("W-05", "East Wing", "ICU", 2, null)));
    }

    private static Javalin appWith(EquipmentFailurePublisher publisher, StaffingBoard board) {
        WardService wards = wards();
        return WardServiceApp.createApp(wards, new EquipmentFailureService(wards, publisher), board).start(0);
    }

    private static HttpResponse<String> get(Javalin app, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(Javalin app, String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Handles: success path. 202 QUEUED, and the queue message has the wardId from the URL.
    @Test
    void shouldAccept202AndQueueAFailure() throws Exception {
        FakePublisher publisher = new FakePublisher();
        Javalin app = appWith(publisher, new StaffingBoard());
        try {
            HttpResponse<String> response = post(app, "/wards/W-05/equipment-failures", VALID_BODY);

            assertEquals(202, response.statusCode());
            assertTrue(response.body().contains("QUEUED"));
            assertEquals(List.of(new EquipmentAlertMessage("VENT-12", "W-05", "Lost power", Severity.CRITICAL)),
                    publisher.published);
        } finally {
            app.stop();
        }
    }

    // Handles: NoSuchElementException branch. Unknown ward -> 404, nothing queued.
    @Test
    void shouldAnswer404ForAnUnknownWard() throws Exception {
        FakePublisher publisher = new FakePublisher();
        Javalin app = appWith(publisher, new StaffingBoard());
        try {
            HttpResponse<String> response = post(app, "/wards/W-99/equipment-failures", VALID_BODY);

            assertEquals(404, response.statusCode());
            assertTrue(response.body().contains("W-99"));
            assertTrue(publisher.published.isEmpty());
        } finally {
            app.stop();
        }
    }

    // Handles: IllegalArgumentException branch plus the bodyAsClass() catch. A missing field,
    // bad JSON and an unknown severity all answer 400 and queue nothing.
    @Test
    void shouldAnswer400ForInvalidBodies() throws Exception {
        FakePublisher publisher = new FakePublisher();
        Javalin app = appWith(publisher, new StaffingBoard());
        try {
            HttpResponse<String> missing = post(app, "/wards/W-05/equipment-failures",
                    "{\"description\":\"Lost power\",\"severity\":\"HIGH\"}");
            assertEquals(400, missing.statusCode());
            assertTrue(missing.body().contains("equipmentId"));

            assertEquals(400, post(app, "/wards/W-05/equipment-failures", "not json").statusCode());
            assertEquals(400, post(app, "/wards/W-05/equipment-failures",
                    "{\"equipmentId\":\"V\",\"description\":\"x\",\"severity\":\"PANIC\"}").statusCode());
            assertTrue(publisher.published.isEmpty());
        } finally {
            app.stop();
        }
    }

    // Handles: EquipmentFailurePublishException branch. Broker down -> 503 "NOT queued".
    @Test
    void shouldAnswer503WhenTheBrokerIsUnavailable() throws Exception {
        EquipmentFailurePublisher broken = message -> {
            throw new EquipmentFailurePublisher.EquipmentFailurePublishException("broker down", new RuntimeException());
        };
        Javalin app = appWith(broken, new StaffingBoard());
        try {
            HttpResponse<String> response = post(app, "/wards/W-05/equipment-failures", VALID_BODY);

            assertEquals(503, response.statusCode());
            assertTrue(response.body().contains("NOT queued"));
        } finally {
            app.stop();
        }
    }

    // Handles: GET /wards/{id}/staffing. 404 until an update arrives, then 200 with the schedule.
    @Test
    void shouldServeTheLatestStaffingUpdate() throws Exception {
        StaffingBoard board = new StaffingBoard();
        Javalin app = appWith(new FakePublisher(), board);
        try {
            assertEquals(404, get(app, "/wards/W-05/staffing").statusCode());

            board.record(new StaffingUpdate("W-05", "ICU", 8, "CODE BLUE", 5));

            HttpResponse<String> response = get(app, "/wards/W-05/staffing");
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("CODE BLUE"));
        } finally {
            app.stop();
        }
    }

    // Handles: the one-argument createApp(service) used by the older tests has neither MQ route.
    @Test
    void shouldNotHaveTheMqRoutesWithoutTheirCollaborators() throws Exception {
        Javalin app = WardServiceApp.createApp(wards()).start(0);
        try {
            assertEquals(404, get(app, "/wards/W-05/staffing").statusCode());
            assertEquals(404, post(app, "/wards/W-05/equipment-failures", VALID_BODY).statusCode());
        } finally {
            app.stop();
        }
    }
}