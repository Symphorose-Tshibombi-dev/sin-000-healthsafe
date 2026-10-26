package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The part of a ward that staffing-service cares about.
 * Extra fields ward-service sends (bedsAvailable, notes) are ignored on purpose.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WardInfo(String wardId, String wing, String department) {
}