package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.mq.MqConfig;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

public class EquipmentAlertServiceApp {

    public static void main(String[] args) throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        factory.getRedeliveryPolicy().setMaximumRedeliveries(3);
        factory.getRedeliveryPolicy().setInitialRedeliveryDelay(1000);

        EquipmentAlertService service = new EquipmentAlertService(new JmsAlertPublisher(factory, MqConfig.QUEUE));

        EquipmentAlertConsumer consumer = new EquipmentAlertConsumer(factory, MqConfig.QUEUE, service);
        try {
            consumer.start();
        } catch (Exception error) {
            System.err.println("Cannot reach the ActiveMQ broker at " + MqConfig.BROKER_URL
                    + ". Start it first: cd common && docker compose up -d");
            throw error;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                consumer.close();
            } catch (Exception ignored) {
                // shutting down anyway
            }
        }));

        createApp(service).start(7034);
    }


}

// MQ TODO: consumes ActiveMQ queue MqConfig.QUEUE at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
// Producer: ward-service publishes here when it detects an equipment failure on one of its wards.
