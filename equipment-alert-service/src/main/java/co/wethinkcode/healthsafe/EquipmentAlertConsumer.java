package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.ConnectionFactory;

import javax.jms.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static jdk.internal.misc.Signal.handle;

public class EquipmentAlertConsumer implements AutoCloseable{

    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final EquipmentAlertService service;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> finishedMessageIds = ConcurrentHashMap.newKeySet();

    private Connection connection;
    private Session session;

    public EquipmentAlertConsumer(ConnectionFactory connectionFactory, String queueName, EquipmentAlertService service) {
        this.connectionFactory = connectionFactory;
        this.queueName = queueName;
        this.service = service;
    }

    public void start() throws JMSException {
        connection = connectionFactory.createConnection();
        session = connection.createSession(true, Session.SESSION_TRANSACTED);
        MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));
        consumer.setMessageListener(this::onMessage);
        connection.start();
    }
    private void onMessage(Message message) {
        try {
            handle(message);
            session.commit();
        } catch (Exception error) {
            System.err.println("Equipment alert failed, will be redelivered: " + error.getMessage());
            try {
                session.rollback();
            } catch (JMSException rollbackError) {
                System.err.println("Rollback failed: " + rollbackError.getMessage());
            }
        }
    }

}
