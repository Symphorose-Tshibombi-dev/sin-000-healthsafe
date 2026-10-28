package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.exceptions.StaffingEventPublisher;
import com.fasterxml.jackson.databind.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.*;

import javax.jms.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Tests: JmsStaffingEventPublisher.publish() against a real in-memory ActiveMQ broker. */
public class JmsStaffingEventPublisherTest {

    private static final String TOPIC = "staffing-events-topic";
    private static final Schedule SCHEDULE = new Schedule("W-01", "Cardiology", 5, "ELEVATED", 2);

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
    }

    // Handles: publish() sends the schedule as JSON with all five fields.
    @Test
    void shouldPublishTheScheduleAsJson() throws Exception {
        Subscriber subscriber = new Subscriber(factory);
        try {
            new JmsStaffingEventPublisher(factory, TOPIC).publish(SCHEDULE);

            TextMessage message = (TextMessage) subscriber.consumer.receive(3000);
            assertNotNull(message);
            JsonNode json = new ObjectMapper().readTree(message.getText());
            assertEquals("W-01", json.get("wardId").asText());
            assertEquals("Cardiology", json.get("department").asText());
            assertEquals(5, json.get("alertLevel").asInt());
            assertEquals("ELEVATED", json.get("mode").asText());
            assertEquals(2, json.get("doctorsOnCall").asInt());
        } finally {
            subscriber.connection.close();
        }
    }

    // Handles: topic semantics. Two subscribers each receive their own copy.
    @Test
    void shouldDeliverToEverySubscriber() throws Exception {
        Subscriber first = new Subscriber(factory);
        Subscriber second = new Subscriber(factory);
        try {
            new JmsStaffingEventPublisher(factory, TOPIC).publish(SCHEDULE);

            assertNotNull(first.consumer.receive(3000));
            assertNotNull(second.consumer.receive(3000));
        } finally {
            first.connection.close();
            second.connection.close();
        }
    }

    // Handles: publish() catch block. Nothing listens on port 1, so it throws StaffingEventPublishException.
    @Test
    void shouldThrowWhenTheBrokerIsUnreachable() {
        JmsStaffingEventPublisher publisher = new JmsStaffingEventPublisher(new ActiveMQConnectionFactory("tcp://localhost:1"), TOPIC);

        assertThrows(StaffingEventPublisher.StaffingEventPublishException.class, () -> publisher.publish(SCHEDULE));
    }
}