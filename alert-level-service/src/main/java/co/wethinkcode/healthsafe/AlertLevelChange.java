package co.wethinkcode.healthsafe;

/**
 * The JSON body of every message on alert-level-topic:
 * {"previousLevel":2,"level":5,"changedAt":"2026-10-27T09:30:00Z"}
 */
public record AlertLevelChange(int previousLevel, int level, String changedAt) {
}