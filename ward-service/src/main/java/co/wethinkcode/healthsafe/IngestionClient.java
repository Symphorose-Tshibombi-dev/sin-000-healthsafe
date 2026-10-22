package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;

public class IngestionClient {
    private final String baseUrl;

    public IngestionClient(String baseUrl){
        this.baseUrl = baseUrl;
    }
    public List<Ward> fetchWards() {
        try {
            HttpClient http = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/wards"))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200){
                throw new IllegalStateException("ingestion-service answered with status" + response.statusCode());

            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), new TypeReference<List<Ward>>() {
            });
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Could not reach ingestion-service", e);
        }
    }
}
