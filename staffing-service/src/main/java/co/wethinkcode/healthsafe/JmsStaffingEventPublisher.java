package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.exceptions.StaffingEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Publishes a schedule to the staffing-events TOPIC. Every subscriber gets its own copy.
 * Non-persistent on purpose: an event is "this ward's staffing is now X", and the next
 * change replaces it, so losing one for an offline subscriber is acceptable.
 * One short-lived connection per event, so the service can start before the broker.
 */
public class JmsStaffingEventPublisher implements StaffingEventPublisher {

    private final ConnectionFactory connectionFactory;
    private final String topicName;
    private final ObjectMapper mapper = new ObjectMapper();

    public JmsStaffingEventPublisher(ConnectionFactory connectionFactory, String topicName) {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
    }

    @Override
    public void publish(Schedule schedule) {
        Connection connection = null;
        try {
            String json = mapper.writeValueAsString(schedule);
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createTopic(topicName));
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            producer.send(session.createTextMessage(json));
        } catch (Exception error) {
            throw new StaffingEventPublishException("Could not publish staffing event: " + error.getMessage(), error);
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