package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceConfigTest {
    @Test
    void shouldReadIngestionUrlFromProperties() {
        assertEquals("http://localhost:7030", ServiceConfig.ingestionUrl());
    }
}
