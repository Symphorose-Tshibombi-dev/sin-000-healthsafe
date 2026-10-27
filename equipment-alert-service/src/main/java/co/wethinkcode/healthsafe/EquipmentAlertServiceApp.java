package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.mq.MqConfig;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Map;

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

    public static Javalin createApp(EquipmentAlertService service) {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        app.post("/equipment-alerts", ctx -> {
            EquipmentAlert alert;
            try {
                alert = ctx.bodyAsClass(EquipmentAlert.class);
            } catch (Exception error) {
                ctx.status(400).json(Map.of("error",
                        "Body must be JSON like {\"equipmentId\":\"VENT-12\",\"wardId\":\"W-05\","
                                + "\"description\":\"Lost power\",\"severity\":\"CRITICAL\"} "
                                + "with severity LOW, MEDIUM, HIGH or CRITICAL"));
                return;
            }

            try {
                service.submit(alert);
                ctx.status(202).json(Map.of("status", "QUEUED", "equipmentId", alert.equipmentId()));
            } catch (IllegalArgumentException error) {
                ctx.status(400).json(Map.of("error", error.getMessage()));
            } catch (AlertPublisher.AlertPublishException error) {
                ctx.status(503).json(Map.of("error", "Alert was NOT queued, the message broker is unavailable"));
            }
        });

        app.get("/equipment-alerts", ctx -> ctx.json(service.processedAlerts()));

        return app;
    }
}

// MQ TODO: consumes ActiveMQ queue MqConfig.QUEUE at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
// Producer: ward-service publishes here when it detects an equipment failure on one of its wards.
