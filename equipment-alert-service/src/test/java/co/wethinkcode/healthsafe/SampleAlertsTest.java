package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class SampleAlertsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static JsonNode samples;

    @BeforeAll
    static void loadSamples() throws Exception {
        try (InputStream in = SampleAlertsTest.class.getResourceAsStream("/sample-alerts.json")) {
            assertNotNull(in, "sample-alerts.json not found on the test classpath");
            samples = MAPPER.readTree(in);
        }
    }

    @Test
    void shouldAcceptEveryValidSample() throws Exception {
        assertTrue(samples.get("valid").size() > 0);

        for (JsonNode node : samples.get("valid")){
            EquipmentAlertServiceTest alert = MAPPER.treeToValue(node, EquipmentAlertServiceTest.class);

            assertDoesNotThrow(() -> EquipmentAlertVaildator.validate(alert), node.toString());
        }
    }

    @Test
    void shouldRejectEveryInvalidSampleAndNameTheBadField() throws Exception {
        assertTrue(samples.get("invalid").size() > 0);

        for (JsonNode entry : samples.get("invalid")) {
            EquipmentAlertServiceTest alert = MAPPER.treeToValue(entry.get("alert"), EquipmentAlertServiceTest.class);
            String expectedField = entry.get("expectedError").asText();

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> EquipmentAlertValidator.validate(alert), entry.toString());

            assertTrue(error.getMessage().contains(expectedField),
                    "expected the error to mention " + expectedField + " but was: " + error.getMessage());
        }
}
