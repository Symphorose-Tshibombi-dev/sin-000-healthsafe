package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Subscribes to the staffing-events TOPIC and keeps the latest update per ward on a board.
 *
 * Topic semantics: every ward-service instance gets its own copy of every event, and a
 * subscriber that is not connected misses what was sent meanwhile (non-durable). That is
 * fine here because the next event for a ward replaces the previous one.
 * A malformed message is logged and skipped, since there is nobody to redeliver it to.
 */
public class StaffingEventListener implements AutoCloseable {

    private final ConnectionFactory connectionFactory;
    private final String topicName;
    private final StaffingBoard board;
    private final ObjectMapper mapper = new ObjectMapper();

    private Connection connection;

    public StaffingEventListener(ConnectionFactory connectionFactory, String topicName, StaffingBoard board) {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
        this.board = board;
    }

    public void start() throws JMSException {
        connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createTopic(topicName));
        consumer.setMessageListener(this::onMessage);
        connection.start();
    }

    private void onMessage(Message message) {
        try {
            if (!(message instanceof TextMessage text)) {
                throw new IllegalArgumentException("Expected a JSON text message");
            }
            StaffingUpdate update = mapper.readValue(text.getText(), StaffingUpdate.class);
            if (update.wardId() == null || update.wardId().isBlank()) {
                throw new IllegalArgumentException("wardId is missing");
            }
            board.record(update);
            System.out.println("STAFFING UPDATE for " + update.wardId() + ": " + update);
        } catch (Exception error) {
            System.err.println("Ignoring a bad staffing event: " + error.getMessage());
        }
    }

    @Override
    public void close() throws JMSException {
        if (connection != null) {
            connection.close();
        }
    }
}