package co.wethinkcode.healthsafe;

public interface AlertLevelSource {

    /**
     * @throws DownstreamUnavailableException if alert-level-service cannot be used
     */
    int currentLevel();
}
