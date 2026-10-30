package co.wethinkcode.healthsafe;

public record EquipmentAlertMessage(String equipmentId, String wardId, String description, Severity severity) {
}