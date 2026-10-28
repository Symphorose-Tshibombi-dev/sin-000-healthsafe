package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.mq.MqConfig;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class WardServiceApp {

    public static void main(String[] args) {

        IngestionClient ingestion = new IngestionClient(ServiceConfig.ingestionUrl());

        List<Ward> wards;
        try {
            wards = ingestion.fetchWards();
        } catch (IllegalStateException error) {
            System.err.println("Cannot start ward-service: " + error.getMessage());

            if (error.getCause() != null) {
                System.err.println("Reason: " + error.getCause());
            }

            System.exit(1);
            return;
        }

        System.out.println("Loaded " + wards.size() + " wards from ingestion-service");
        WardService wardService = new WardService(wards);

        // The REST part must keep working without the broker, so MQ problems are only warnings here.
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        EquipmentFailureService failures = new EquipmentFailureService(
                wardService, new JmsEquipmentFailurePublisher(factory, MqConfig.QUEUE));

        StaffingBoard board = new StaffingBoard();
        StaffingEventListener listener = new StaffingEventListener(factory, MqConfig.TOPIC, board);
        try {
            listener.start();
        } catch (Exception error) {
            System.err.println("WARNING: cannot reach the ActiveMQ broker at " + MqConfig.BROKER_URL
                    + ", so staffing updates are NOT being received. Start it: cd common && docker compose up -d");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                listener.close();
            } catch (Exception ignored) {
                // shutting down anyway
            }
        }));

        createApp(wardService, failures, board).start(7031);

    }

    /** REST only: no equipment-failure route and no staffing route. */
    public static Javalin createApp(WardService service) {
        return createApp(service, null, null);
    }

    public static Javalin createApp(WardService service, EquipmentFailureService failures, StaffingBoard board) {
        Javalin app = Javalin.create();

        app.get("/wards/{id}", ctx -> {
            String id = ctx.pathParam("id");
            Optional<Ward> ward = service.findById(id);

            if (ward.isPresent()) {
                ctx.json(ward.get());
            } else {
                ctx.status(404).json(Map.of("error", "Ward " + id + " not found"));
            }
        });

        app.get("/wards", ctx -> ctx.json(service.findAll()));

        app.get("/departments", ctx -> ctx.json(service.findDepartments()));
        app.get("/health", ctx -> ctx.result("OK"));

        if (failures != null) {
            // Stage 4: a failure reported on one of this service's wards goes to equipment-failure-queue.
            app.post("/wards/{id}/equipment-failures", ctx -> {
                String id = ctx.pathParam("id");
                EquipmentFailureReport report;
                try {
                    report = ctx.bodyAsClass(EquipmentFailureReport.class);
                } catch (Exception error) {
                    ctx.status(400).json(Map.of("error",
                            "Body must be JSON like {\"equipmentId\":\"VENT-12\",\"description\":\"Lost power\","
                                    + "\"severity\":\"CRITICAL\"} with severity LOW, MEDIUM, HIGH or CRITICAL"));
                    return;
                }

                try {
                    EquipmentAlertMessage sent = failures.report(id, report);
                    ctx.status(202).json(Map.of("status", "QUEUED", "wardId", id, "equipmentId", sent.equipmentId()));
                } catch (NoSuchElementException error) {
                    ctx.status(404).json(Map.of("error", error.getMessage()));
                } catch (IllegalArgumentException error) {
                    ctx.status(400).json(Map.of("error", error.getMessage()));
                } catch (EquipmentFailurePublisher.EquipmentFailurePublishException error) {
                    ctx.status(503).json(Map.of("error", "Equipment failure was NOT queued, the message broker is unavailable"));
                }
            });
        }

        if (board != null) {
            // Stage 3: the latest staffing update received from the topic, so no polling is needed.
            app.get("/wards/{id}/staffing", ctx -> {
                String id = ctx.pathParam("id");
                Optional<StaffingUpdate> update = board.latestFor(id);

                if (update.isPresent()) {
                    ctx.json(update.get());
                } else {
                    ctx.status(404).json(Map.of("error", "No staffing update received yet for ward " + id));
                }
            });
        }

        return app;
    }
}

// MQ: subscribes to the ActiveMQ topic MqConfig.TOPIC (StaffingEventListener) and publishes to the
// ActiveMQ queue MqConfig.QUEUE (JmsEquipmentFailurePublisher) when a failure is reported on one of its wards.