package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests: JmsAlertLevelPublisher.publish() against a real in-memory ActiveMQ broker.
 * Shows TOPIC behaviour: every subscriber gets a copy, and nothing is kept for late subscribers.
 */
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

    @Test
    void shouldDeliverAChangeToASubscriber() throws Exception {
        Subscriber subscriber = new Subscriber(factory);
        try {
            new JmsAlertLevelPublisher(factory, TOPIC).publish(CHANGE);

            TextMessage message = subscriber.receive(3000);
            assertNotNull(message, "the subscriber should receive the change");
            JsonNode json = new ObjectMapper().readTree(message.getText());
            assertEquals(2, json.get("previousLevel").asInt());
            assertEquals(5, json.get("level").asInt());
            assertEquals("2026-10-27T09:30:00Z", json.get("changedAt").asText());
        } finally {
            subscriber.close();
        }
    }

    // Handles: topic semantics. Two subscribers each receive their own copy of the same change.
    @Test
    void shouldDeliverTheSameChangeToEverySubscriber() throws Exception {
        Subscriber first = new Subscriber(factory);
        Subscriber second = new Subscriber(factory);
        try {
            new JmsAlertLevelPublisher(factory, TOPIC).publish(CHANGE);

            assertNotNull(first.receive(3000));
            assertNotNull(second.receive(3000));
        } finally {
            first.close();
            second.close();
        }
    }

    // Handles: non-persistent topic. Publishing with nobody listening is fine, and a subscriber
    // that connects afterwards does not get the old change.
    @Test
    void shouldNotKeepAChangeForALateSubscriber() throws Exception {
        new JmsAlertLevelPublisher(factory, TOPIC).publish(CHANGE);

        Subscriber late = new Subscriber(factory);
        try {
            assertNull(late.receive(300));
        } finally {
            late.close();
        }
    }

    // Handles: publish() catch block. Nothing listens on port 1, so it throws
    // AlertLevelPublishException (which the PUT route logs instead of failing).
    @Test
    void shouldThrowWhenTheBrokerIsUnreachable() {
        JmsAlertLevelPublisher publisher = new JmsAlertLevelPublisher(new ActiveMQConnectionFactory("tcp://localhost:1"), TOPIC);

        assertThrows(AlertLevelPublisher.AlertLevelPublishException.class, () -> publisher.publish(CHANGE));
    }
}