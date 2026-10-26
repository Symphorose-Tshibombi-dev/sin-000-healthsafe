package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
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
        }
        return mapper.readValue(response.body(), WardInfo.class);
    }
}