package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.exceptions.DownstreamUnavailableException;
import co.wethinkcode.healthsafe.exceptions.WardNotFoundException;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WardClientTest {

    @Test
    void shouldFindAWardAndIgnoreExtraFields() {
        Javalin fakeWardService = Javalin.create()
                .get("/wards/W-01", ctx -> ctx.json(Map.of(
                        "wardId", "W-01",
                        "wing", "East Wing",
                        "department", "Cardiology",
                        "bedsAvailable", 3,
                        "notes", "anything")))
                .start(0);

        try {
            WardClient client = new WardClient("http://localhost:" + fakeWardService.port());

            WardInfo ward = client.findWard("W-01");

            assertEquals("W-01", ward.wardId());
            assertEquals("Cardiology", ward.department());
        } finally {
            fakeWardService.stop();
        }
    }

    @Test
    void shouldThrowWardNotFoundWhenWardServiceAnswers404() {
        Javalin fakeWardService = Javalin.create()
                .get("/wards/{id}", ctx -> ctx.status(404).json(Map.of("error", "not found")))
                .start(0);

        try {
            WardClient client = new WardClient("http://localhost:" + fakeWardService.port());

            WardNotFoundException error = assertThrows(WardNotFoundException.class, () -> client.findWard("W-99"));

            assertTrue(error.getMessage().contains("W-99"));
        } finally {
            fakeWardService.stop();
        }
    }

    @Test
    void shouldThrowDownstreamUnavailableWhenWardServiceAnswersWithAnError() {
        Javalin fakeWardService = Javalin.create()
                .get("/wards/{id}", ctx -> ctx.status(500))
                .start(0);

        try {
            WardClient client = new WardClient("http://localhost:" + fakeWardService.port());

            DownstreamUnavailableException error =
                    assertThrows(DownstreamUnavailableException.class, () -> client.findWard("W-01"));

            assertTrue(error.getMessage().contains("500"));
        } finally {
            fakeWardService.stop();
        }
    }

    @Test
    void shouldThrowDownstreamUnavailableWhenWardServiceIsNotRunning() {
        WardClient client = new WardClient("http://localhost:1");

        DownstreamUnavailableException error =
                assertThrows(DownstreamUnavailableException.class, () -> client.findWard("W-01"));

        assertTrue(error.getMessage().contains("ward-service"));
    }
}