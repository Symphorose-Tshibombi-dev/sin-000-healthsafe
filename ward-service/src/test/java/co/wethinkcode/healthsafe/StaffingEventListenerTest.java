package co.wethinkcode.healthsafe;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.Connection;
import javax.jms.Session;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests: StaffingEventListener.start() / onMessage() against a real in-memory ActiveMQ broker.
 * The "staffing-service" side is played by sendEvent(), which publishes JSON on the topic.
 */
public class StaffingEventListenerTest {

    private static final String TOPIC = "staffing-events-topic";

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

    private static void waitFor(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(25);
        }
    }

    // helper: publish raw text on the topic (can be good or bad JSON)
    private void sendEvent(String body) throws Exception {
        Connection connection = factory.createConnection();
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            session.createProducer(session.createTopic(TOPIC)).send(session.createTextMessage(body));
        } finally {
            connection.close();
        }
    }

    private static final String W01_EVENT =
            "{\"wardId\":\"W-01\",\"department\":\"Cardiology\",\"alertLevel\":5,\"mode\":\"ELEVATED\",\"doctorsOnCall\":2}";

    // Handles: a published schedule lands on the board and can be read back per ward.
    @Test
    void shouldRecordAnUpdateOnTheBoard() throws Exception {
        StaffingBoard board = new StaffingBoard();
        try (StaffingEventListener listener = new StaffingEventListener(factory, TOPIC, board)) {
            listener.start();

            sendEvent(W01_EVENT);

            waitFor(() -> board.latestFor("W-01").isPresent());
            StaffingUpdate update = board.latestFor("W-01").orElseThrow();
            assertEquals("ELEVATED", update.mode());
            assertEquals(2, update.doctorsOnCall());
            assertTrue(board.latestFor("W-99").isEmpty());
        }
    }

    // Handles: a newer update for the same ward replaces the older one.
    @Test
    void shouldKeepOnlyTheLatestUpdatePerWard() throws Exception {
        StaffingBoard board = new StaffingBoard();
        try (StaffingEventListener listener = new StaffingEventListener(factory, TOPIC, board)) {
            listener.start();

            sendEvent(W01_EVENT);
            sendEvent("{\"wardId\":\"W-01\",\"department\":\"Cardiology\",\"alertLevel\":8,\"mode\":\"CODE BLUE\",\"doctorsOnCall\":4}");

            waitFor(() -> board.latestFor("W-01").map(u -> u.alertLevel() == 8).orElse(false));
            assertEquals("CODE BLUE", board.latestFor("W-01").orElseThrow().mode());
        }
    }

    // Handles: onMessage() catch. Bad JSON and a missing wardId are skipped, and the good
    // event sent after them is still processed.
    @Test
    void shouldSkipBadEventsAndKeepGoing() throws Exception {
        StaffingBoard board = new StaffingBoard();
        try (StaffingEventListener listener = new StaffingEventListener(factory, TOPIC, board)) {
            listener.start();

            sendEvent("this is not json");
            sendEvent("{\"department\":\"Cardiology\",\"alertLevel\":5,\"mode\":\"ELEVATED\",\"doctorsOnCall\":2}");
            sendEvent(W01_EVENT);

            waitFor(() -> board.latestFor("W-01").isPresent());
            assertTrue(board.latestFor("W-01").isPresent());
        }
    }

    // Handles: topic semantics. Two ward-service instances each receive their own copy.
    @Test
    void shouldGiveEveryListenerItsOwnCopy() throws Exception {
        StaffingBoard first = new StaffingBoard();
        StaffingBoard second = new StaffingBoard();
        try (StaffingEventListener a = new StaffingEventListener(factory, TOPIC, first);
             StaffingEventListener b = new StaffingEventListener(factory, TOPIC, second)) {
            a.start();
            b.start();

            sendEvent(W01_EVENT);

            waitFor(() -> first.latestFor("W-01").isPresent() && second.latestFor("W-01").isPresent());
            assertTrue(first.latestFor("W-01").isPresent());
            assertTrue(second.latestFor("W-01").isPresent());
        }
    }

    // Handles: the "ignoreUnknown" annotation. Extra fields from staffing-service do not break parsing.
    @Test
    void shouldIgnoreExtraFieldsInTheEvent() throws Exception {
        StaffingBoard board = new StaffingBoard();
        try (StaffingEventListener listener = new StaffingEventListener(factory, TOPIC, board)) {
            listener.start();

            sendEvent("{\"wardId\":\"W-01\",\"department\":\"Cardiology\",\"alertLevel\":5,\"mode\":\"ELEVATED\","
                    + "\"doctorsOnCall\":2,\"somethingNew\":true}");

            waitFor(() -> board.latestFor("W-01").isPresent());
            assertTrue(board.latestFor("W-01").isPresent());
        }
    }
}