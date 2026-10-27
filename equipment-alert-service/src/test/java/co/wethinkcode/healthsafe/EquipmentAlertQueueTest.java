package co.wethinkcode.healthsafe;

import javax.jms.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import javax.print.attribute.standard.Severity;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import static org.junit.jupiter.api.Assertions.*;

public class EquipmentAlertQueueTest {
    private static final String QUEUE = "equipment-failure-queue";

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
        factory.getRedeliveryPolicy().setMaximumRedeliveries(2);
        factory.getRedeliveryPolicy().setInitialRedeliveryDelay(50);
        factory.getRedeliveryPolicy().setRedeliveryDelay(50);
    }

    @AfterEach
    void stopBroker() throws Exception {
        broker.stop();
        broker.waitUntilStopped();
    }

    private void sendRaw(String body) throws Exception {
        Connection connection = factory.createConnection();
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            session.createProducer(session.createQueue(QUEUE)).send(session.createTextMessage(body));
        } finally {
            connection.close();
        }
    }

    private static EquipmentAlert alert(String equipmentId) {
        return new EquipmentAlert(equipmentId, "W-05", "Lost power", Severity.CRITICAL);
    }

    @Test
    void shouldProcessAnAlertSentThroughTheQueue() throws Exception {
        EquipmentAlertService service = new EquipmentAlertService(new JmsAlertPublisher(factory, QUEUE));
        try (EquipmentAlertConsumer consumer = new EquipmentAlertConsumer(factory, QUEUE, service)) {
            consumer.start();

            service.submit(alert("VENT-12"));

            waitFor(() -> service.processedAlerts().size() == 1);
            assertEquals(alert("VENT-12"), service.processedAlerts().get(0).alert());
        }
    }

    @Test
    void shouldKeepAlertsWhileTheConsumerIsDownAndProcessThemOnStart() throws Exception {
        EquipmentAlertService service = new EquipmentAlertService(new JmsAlertPublisher(factory, QUEUE));

        service.submit(alert("VENT-1"));
        service.submit(alert("PUMP-2"));
        assertTrue(service.processedAlerts().isEmpty());

        try (EquipmentAlertConsumer consumer = new EquipmentAlertConsumer(factory, QUEUE, service)) {
            consumer.start();

            waitFor(() -> service.processedAlerts().size() == 2);
            assertEquals(2, service.processedAlerts().size());
        }
    }

    @Test
    void shouldGiveEachAlertToOnlyOneOfTwoConsumers() throws Exception {
        EquipmentAlertService first = new EquipmentAlertService(new JmsAlertPublisher(factory, QUEUE));
        EquipmentAlertService second = new EquipmentAlertService(new JmsAlertPublisher(factory, QUEUE));
        try (EquipmentAlertConsumer a = new EquipmentAlertConsumer(factory, QUEUE, first);
             EquipmentAlertConsumer b = new EquipmentAlertConsumer(factory, QUEUE, second)) {
            a.start();
            b.start();

            for (int i = 0; i < 10; i++) {
                first.submit(alert("DEV-" + i));
            }

            waitFor(() -> first.processedAlerts().size() + second.processedAlerts().size() >= 10);
            Thread.sleep(200); // would reveal any duplicate delivery
            assertEquals(10, first.processedAlerts().size() + second.processedAlerts().size());
        }
    }



}
