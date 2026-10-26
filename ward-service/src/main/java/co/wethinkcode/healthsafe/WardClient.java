package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.*;
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
}
