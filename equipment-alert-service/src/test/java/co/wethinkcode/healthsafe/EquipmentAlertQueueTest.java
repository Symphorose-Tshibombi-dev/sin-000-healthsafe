package co.wethinkcode.healthsafe;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.*;

import java.util.UUID;

public class EquipmentAlertQueueTest {
    private static final String QUEUE = "equipment-failure-queue";

    private BrokerService broker;
    private ActiveMQConnectionFactory factory;

    @BeforeEach
    void startBroker() throws Exception {
        String name = "test-" + UUID.randomUUID();
        broker = new BrokerService();
        broker.setBrokerName(name);
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.start();
        broker.waitUntilStarted();

        factory = new ActiveMQConnectionFactory("vm://" + name + "?create=false");
        factory.getRedeliveryPolicy().setMaximumRedeliveries(2);
        factory.getRedeliveryPolicy().setInitialRedeliveryDelay(50);
        factory.getRedeliveryPolicy().setRedeliveryDelay(50);
    }
}
