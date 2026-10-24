package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceConfigTest {

    @Test
    void shouldReadWardUrl() {
        assertEquals("http://localhost:7031", ServiceConfig.wardUrl());
    }

    @Test
    void shouldReadAlertLevelUrl() {
        assertEquals("http://localhost:7032", ServiceConfig.alertLevelUrl());
    }

}
