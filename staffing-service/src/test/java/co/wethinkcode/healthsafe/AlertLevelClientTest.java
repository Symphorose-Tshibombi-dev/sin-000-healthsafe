package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AlertLevelClientTest {

    @Test
    void shouldReadTheCurrentLevel() {
        Javalin fakeAlertService = Javalin.create()
                .get("/alert-level", ctx -> ctx.json(Map.of("level", 5)))
                .start(0);

        try {
            AlertLevelClient client = new AlertLevelClient("http://localhost:" + fakeAlertService.port());

            assertEquals(5, client.currentLevel());
        } finally {
            fakeAlertService.stop();
        }
    }

    @Test
    void shouldThrowDownstreamUnavailableWhenAlertServiceAnswersWithAnError() {
        Javalin fakeAlertService = Javalin.create()
                .get("/alert-level", ctx -> ctx.status(500))
                .start(0);

        try {
            AlertLevelClient client = new AlertLevelClient("http://localhost:" + fakeAlertService.port());

            DownstreamUnavailableException error =
                    assertThrows(DownstreamUnavailableException.class, client::currentLevel);

            assertTrue(error.getMessage().contains("500"));
        } finally {
            fakeAlertService.stop();
        }
    }

    @Test
    void shouldThrowDownstreamUnavailableWhenTheLevelIsOutOfRange() {
        Javalin fakeAlertService = Javalin.create()
                .get("/alert-level", ctx -> ctx.json(Map.of("level", 12)))
                .start(0);

        try {
            AlertLevelClient client = new AlertLevelClient("http://localhost:" + fakeAlertService.port());

            assertThrows(DownstreamUnavailableException.class, client::currentLevel);
        } finally {
            fakeAlertService.stop();
        }
    }

    @Test
    void shouldThrowDownstreamUnavailableWhenAlertServiceIsNotRunning() {
        AlertLevelClient client = new AlertLevelClient("http://localhost:1");

        DownstreamUnavailableException error =
                assertThrows(DownstreamUnavailableException.class, client::currentLevel);

        assertTrue(error.getMessage().contains("alert-level-service"));
    }
}