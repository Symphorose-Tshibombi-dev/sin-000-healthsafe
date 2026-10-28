package co.wethinkcode.healthsafe.mq;

/**
 * alert-level-service is a producer on LEVEL_TOPIC: it announces every change of the
 * hospital Emergency Status so interested services can react without polling.
 * Duplicated per service because every module is an independent Maven project.
 */
public final class MqConfig {

    public static final String BROKER_URL = "tcp://localhost:61616";
    public static final String LEVEL_TOPIC = "alert-level-topic";

    private MqConfig() {
    }
}