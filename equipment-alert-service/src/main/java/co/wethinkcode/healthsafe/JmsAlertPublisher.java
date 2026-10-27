package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * Puts each alert on the queue as a PERSISTENT JSON text message, so the broker keeps it
 * until a consumer has processed and acknowledged it.
 * One short-lived connection per alert: simple, and the service can start before the broker.
 */
public class JmsAlertPublisher implements AlertPublisher {

    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final ObjectMapper mapper = new ObjectMapper();

    public JmsAlertPublisher(ConnectionFactory connectionFactory, String queueName) {
        this.connectionFactory = connectionFactory;
        this.queueName = queueName;
    }

    @Override
    public void publish(EquipmentAlert alert) {
        Connection connection = null;
        try {
            String json = mapper.writeValueAsString(alert);
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(session.createTextMessage(json));
        } catch (Exception error) {
            throw new AlertPublishException("Could not queue equipment alert: " + error.getMessage(), error);
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