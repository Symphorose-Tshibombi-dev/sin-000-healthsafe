package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.exceptions.DownstreamUnavailableException;
import co.wethinkcode.healthsafe.exceptions.WardLookup;
import co.wethinkcode.healthsafe.exceptions.WardNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class WardClient implements WardLookup {

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public WardClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public WardInfo findWard(String wardId) {
        String safeId = URLEncoder.encode(wardId, StandardCharsets.UTF_8).replace("+", "%20");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/wards/" + safeId))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new WardNotFoundException(wardId);
            }
            if (response.statusCode() != 200) {
                throw new DownstreamUnavailableException(
                        "ward-service answered with status " + response.statusCode());
            }

            return mapper.readValue(response.body(), WardInfo.class);
        } catch (JsonProcessingException error) {
            throw new DownstreamUnavailableException("ward-service sent data that could not be read", error);
        } catch (IOException error) {
            throw new DownstreamUnavailableException("Could not reach ward-service", error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new DownstreamUnavailableException("Interrupted while calling ward-service", error);
        }
    }
}