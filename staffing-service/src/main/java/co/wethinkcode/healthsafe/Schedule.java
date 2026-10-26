package co.wethinkcode.healthsafe;

public record Schedule(String wardId, String department, int alertLevel, String mode, int doctorsOnCall) {
}
