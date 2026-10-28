package co.wethinkcode.healthsafe;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.*;

import javax.jms.*;
import java.util.UUID;

public class JmsAlertLevelPublisherTest {
    private static final String TOPIC = "alert-level-topic";
    private static final AlertLevelChange CHANGE = new AlertLevelChange(2, 5, "2026-10-27T09:30:00Z");

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
    }

    @AfterEach
    void stopBroker() throws Exception {
        broker.stop();
        broker.waitUntilStopped();
    }


    private static class Subscriber {
        final Connection connection;
        final MessageConsumer consumer;

        Subscriber(ActiveMQConnectionFactory factory) throws Exception {
            connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(session.createTopic(TOPIC));
        }

        TextMessage receive(long millis) throws Exception {
            return (TextMessage) consumer.receive(millis);
        }

        void close() throws Exception {
            connection.close();
        }
    }

}
