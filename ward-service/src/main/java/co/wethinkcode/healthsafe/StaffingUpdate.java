package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** What ward-service reads from staffing-events-topic (the schedule staffing-service broadcast). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StaffingUpdate(String wardId, String department, int alertLevel, String mode, int doctorsOnCall) {
}