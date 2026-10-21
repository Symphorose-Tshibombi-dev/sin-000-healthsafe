package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.util.List;

public class IngestionClientTest {

    @Test

    void shouldFetchWardsFromIngestionService(){
        Javalin fakeIngestion = Javalin.create()
                .get("/wards" , ctx -> ctx.json(List.of(
                        new Ward("W-01", "East Wing", "Cardiology", 3, null),
                        new Ward("W-04", "North Wing", "Oncology", null, "bedsAvailable '-1' was invalid - flagged for follow-up")
                )))
                .start(0);
    }
}
