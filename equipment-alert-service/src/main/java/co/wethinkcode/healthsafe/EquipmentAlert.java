package co.wethinkcode.healthsafe;

/**
 * The JSON shape used both on the REST endpoint and as the body of every message on
 * the equipment-failure-queue, so ward-service can publish the same JSON directly:
 * {"equipmentId":"VENT-12","wardId":"W-05","description":"Ventilator lost power","severity":"CRITICAL"}
 */
public record EquipmentAlert(String equipmentId, String wardId, String description, Severity severity) {
}