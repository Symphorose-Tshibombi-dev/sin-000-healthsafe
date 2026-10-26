package co.wethinkcode.healthsafe.exceptions;

public interface AlertLevelSource {

    /**
     * @throws DownstreamUnavailableException if alert-level-service cannot be used
     */
    int currentLevel();
}
