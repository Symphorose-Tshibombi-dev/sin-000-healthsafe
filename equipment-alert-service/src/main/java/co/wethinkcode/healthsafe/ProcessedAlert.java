package co.wethinkcode.healthsafe;

/** An alert the consumer has finished handling, plus when. */
public record ProcessedAlert(EquipmentAlert alert, String processedAt) {
}