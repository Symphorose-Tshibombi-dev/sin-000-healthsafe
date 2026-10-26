package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class AlertLevelClient implements AlertLevelSource {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LevelResponse(Integer level) {
    }

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public AlertLevelClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public int currentLevel() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/alert-level"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new DownstreamUnavailableException(
                        "alert-level-service answered with status " + response.statusCode());
            }

            Integer level = mapper.readValue(response.body(), LevelResponse.class).level();
            if (level == null || level < 0 || level > 8) {
                throw new DownstreamUnavailableException("alert-level-service sent an invalid level: " + level);
            }
            return level;
        } catch (JsonProcessingException error) {
            throw new DownstreamUnavailableException("alert-level-service sent data that could not be read", error);
        } catch (IOException error) {
            throw new DownstreamUnavailableException("Could not reach alert-level-service", error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new DownstreamUnavailableException("Interrupted while calling alert-level-service", error);
        }
    }
}