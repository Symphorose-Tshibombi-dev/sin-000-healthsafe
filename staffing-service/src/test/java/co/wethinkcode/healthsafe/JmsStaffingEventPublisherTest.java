package co.wethinkcode.healthsafe;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.*;

import javax.jms.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
}
