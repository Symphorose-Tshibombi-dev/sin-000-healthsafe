package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.*;

/**
 * Sends each failure to the QUEUE as a PERSISTENT JSON message, so the broker keeps it
 * until equipment-alert-service has processed it. If the broker cannot be reached this
 * throws, and the caller must tell the client the failure was NOT queued.
 */
public class JmsEquipmentFailurePublisher implements EquipmentFailurePublisher {

    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final ObjectMapper mapper = new ObjectMapper();

    public JmsEquipmentFailurePublisher(ConnectionFactory connectionFactory, String queueName) {
        this.connectionFactory = connectionFactory;
        this.queueName = queueName;
    }

    @Override
    public void publish(EquipmentAlertMessage message) {
        Connection connection = null;
        try {
            String json = mapper.writeValueAsString(message);
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createQueue(queueName));
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(session.createTextMessage(json));
        } catch (Exception error) {
            throw new EquipmentFailurePublishException("Could not queue equipment failure: " + error.getMessage(), error);
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