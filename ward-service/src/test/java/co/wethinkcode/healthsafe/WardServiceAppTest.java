package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;

public class WardServiceAppTest {

    @Test
    void mustReturnWardWhenIdExists() throws Exception{
        Javalin app = WardServiceApp.createApp(new WardService(List.of(
        new Ward("W-01", "East Wing", "Cardiology", 3, null
        ))).start(0);

        try{
            HttpClient http = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:"+ app.port() + "/ward/W-01"))
                    .GET()
                    .build();

    }
}
