package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WardServiceAppTest {

    @Test
    void mustReturnWardWhenIdExists() throws Exception {
        Javalin app = WardServiceApp.createApp(new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null)
        ))).start(0);

        try {
            HttpClient http = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/wards/W-01"))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Cardiology"));
        } finally {
            app.stop();
        }
    }

    @Test
    void mustReturn404WhenWardDoesNotExist() throws Exception {
        Javalin app = WardServiceApp.createApp(new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null)
        ))).start(0);

        try {
            HttpClient http = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/wards/W-99"))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
            assertTrue(response.body().contains("W-99"));

        } finally {
            app.stop();
        }


    }

    @Test
    void mustReturnAllWards() throws Exception{
        Javalin app = WardServiceApp.createApp(new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null),
                new Ward("W-04", "North Wing", "Oncology", null, "flagged")
        ))).start(0);

        try {
            HttpClient http = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/wards"))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("W-01"));
            assertTrue(response.body().contains("W-04"));

        } finally {
            app.stop();
        }
    }

    @Test

    void mustReturnDepartments() throws Exception {
        Javalin app = WardServiceApp.createApp( new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null),
                new Ward("W-02", "West Wing", "Cardiology", 5, null),
                new Ward("W-04", "North Wing", "Oncology", null, "flagged")
        ))).start(0);

        try{
            HttpClient http = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/departments"))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());


            assertEquals(200, response.statusCode());

            ObjectMapper mapper = new ObjectMapper();
            List<String> departments = mapper.readValue(response.body(), new TypeReference<List<String>>() {});
            assertEquals(List.of("Cardiology" , "Oncology"), departments);


        }finally{
            app.stop();
        }


    }
}
