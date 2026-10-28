package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.*;

import javax.jms.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests: JmsEquipmentFailurePublisher.publish() against a real in-memory ActiveMQ broker.
 * Shows QUEUE behaviour: the message waits on the broker even though nobody is consuming yet.
 */
public class JmsEquipmentFailurePublisherTest {

    private static final String QUEUE = "equipment-failure-queue";
    private static final EquipmentAlertMessage MESSAGE =
            new EquipmentAlertMessage("VENT-12", "W-05", "Lost power", Severity.CRITICAL);

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

    // Handles: publish() sends a PERSISTENT message whose JSON is exactly what
    // equipment-alert-service expects (equipmentId, wardId, description, severity).
    // The message is read AFTER publishing, proving the queue held it.
    @Test
    void shouldLeavePersistentJsonOnTheQueue() throws Exception {
        new JmsEquipmentFailurePublisher(factory, QUEUE).publish(MESSAGE);

        Connection connection = factory.createConnection();
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(session.createQueue(QUEUE));
            TextMessage received = (TextMessage) consumer.receive(3000);

            assertNotNull(received, "the message should have waited on the queue");
            assertEquals(DeliveryMode.PERSISTENT, received.getJMSDeliveryMode());
            JsonNode json = new ObjectMapper().readTree(received.getText());
            assertEquals("VENT-12", json.get("equipmentId").asText());
            assertEquals("W-05", json.get("wardId").asText());
            assertEquals("Lost power", json.get("description").asText());
            assertEquals("CRITICAL", json.get("severity").asText());
        } finally {
            connection.close();
        }
    }

    // Handles: queue semantics. Two consumers, one message: exactly one of them gets it.
    @Test
    void shouldGiveTheMessageToOnlyOneConsumer() throws Exception {
        new JmsEquipmentFailurePublisher(factory, QUEUE).publish(MESSAGE);

        Connection connection = factory.createConnection();
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer first = session.createConsumer(session.createQueue(QUEUE));
            MessageConsumer second = session.createConsumer(session.createQueue(QUEUE));

            int received = 0;
            if (first.receive(1000) != null) {
                received++;
            }
            if (second.receive(300) != null) {
                received++;
            }
            assertEquals(1, received);
        } finally {
            connection.close();
        }
    }

    // Handles: publish() catch block. Nothing listens on port 1, so it throws
    // EquipmentFailurePublishException (the route answers 503, "NOT queued").
    @Test
    void shouldThrowWhenTheBrokerIsUnreachable() {
        JmsEquipmentFailurePublisher publisher =
                new JmsEquipmentFailurePublisher(new ActiveMQConnectionFactory("tcp://localhost:1"), QUEUE);

        assertThrows(EquipmentFailurePublisher.EquipmentFailurePublishException.class, () -> publisher.publish(MESSAGE));
    }
}