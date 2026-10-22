package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IngestionClientTest {

    @Test

    void shouldFetchWardsFromIngestionService(){
        Javalin fakeIngestion = Javalin.create()
                .get("/wards" , ctx -> ctx.json(List.of(
                        new Ward("W-01", "East Wing", "Cardiology", 3, null),
                        new Ward("W-04", "North Wing", "Oncology", null, "bedsAvailable '-1' was invalid - flagged for follow-up")
                )))
                .start(0);

        try {
            IngestionClient client = new IngestionClient("http://localhost:" + fakeIngestion.port());

            List<Ward> wards = client.fetchWards();

            assertEquals(2, wards.size());
            assertEquals(3,wards.get(0).bedsAvailable());
            assertEquals("W-01", wards.get(0).wardId());
            assertNull(wards.get(1).bedsAvailable());
            assertTrue(wards.get(1).notes().contains("-1"));

        } finally {
            fakeIngestion.stop();
        }

    }

    @Test
    void shouldFailWhenIngestionAnswersWithAnError(){

        Javalin fakeIngestion = Javalin.create()
                .get("/wards" , ctx -> ctx.status(500))
                .start(0);
        try{
            IngestionClient client = new IngestionClient("http://localhost:" + fakeIngestion.port());
            IllegalStateException exception = assertThrows(IllegalStateException.class, client::fetchWards );

            assertTrue(exception.getMessage().contains("500"));
        }finally {
            fakeIngestion.stop();
        }
    }
}
