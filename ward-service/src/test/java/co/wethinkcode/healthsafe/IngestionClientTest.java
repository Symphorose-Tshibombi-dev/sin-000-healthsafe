package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

public class IngestionClientTest {

    @Test

    void shouldFetchWardsFromIngestionService(){
        Javalin fakeIngestion = Javalin.create()
                .get("/ward" , ctx -> ctx.result("hello"))
                .start(0);
    }
}
