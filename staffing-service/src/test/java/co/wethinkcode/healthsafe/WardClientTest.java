package co.wethinkcode.healthsafe;

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

}
