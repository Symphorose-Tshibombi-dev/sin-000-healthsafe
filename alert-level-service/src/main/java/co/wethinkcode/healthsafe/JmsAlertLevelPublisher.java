package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.*;

public class JmsAlertLevelPublisher implements AlertLevelPublisher{

    private final ConnectionFactory connectionFactory;
    private final String topicName;
    private final ObjectMapper mapper = new ObjectMapper();

    public JmsAlertLevelPublisher(ConnectionFactory connectionFactory, String topicName) {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
    }

    @Override
    public void publish(AlertLevelChange change) {
        Connection connection = null;
        try {
            String json = mapper.writeValueAsString(change);
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createTopic(topicName));
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            producer.send(session.createTextMessage(json));
        } catch (Exception error) {
            throw new AlertLevelPublishException("Could not publish alert level change: " + error.getMessage(), error);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ignored) {
                    // nothing useful to do
                }
            }
        }
    }
}
