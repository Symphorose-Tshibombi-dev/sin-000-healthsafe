package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WardServiceAppTest {

    @Test
    void mustReturnWardWhenIdExists() throws Exception{
        Javalin app = WardServiceApp.createApp(new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null)
        ))).start(0);

        try{
            HttpClient http = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/wards/W-01"))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request,HttpResponse.BodyHandlers.ofString());

            assertEquals(200,response.statusCode());
            assertTrue(response.body().contains("Cardiology"));
    }finally {
            app.stop();
        }
    }

    @Test

    void mustReturn404WhenWardDoesNotExist() throws Exception{
        Javalin app = WardServiceApp.createApp(new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null)
        ))).start(0);

        try{
            HttpClient http = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/wards/W-99"))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request,HttpResponse.BodyHandlers.ofString());

            assertEquals(404,response.statusCode());

        }finally {
            app.stop();
        }
    }
}
