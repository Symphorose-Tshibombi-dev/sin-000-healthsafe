package co.wethinkcode.healthsafe;

public class AlertLevelService {

    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 8;

    private volatile int level = 0;

    public int getLevel() {
        return level;
    }

    public void setLevel(int newLevel) {
        if (newLevel < MIN_LEVEL || newLevel > MAX_LEVEL) {
            throw new IllegalArgumentException(
                    "Alert level must be between " + MIN_LEVEL + " and " + MAX_LEVEL + ", got " + newLevel);
        }
        level = newLevel;
    }
}
