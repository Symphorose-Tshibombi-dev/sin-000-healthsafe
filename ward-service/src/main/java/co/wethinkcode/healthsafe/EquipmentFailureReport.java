package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Body of POST /wards/{id}/equipment-failures. The ward comes from the URL, not the body. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EquipmentFailureReport(String equipmentId, String description, Severity severity) {
}