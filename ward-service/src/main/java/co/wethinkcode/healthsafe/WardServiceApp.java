package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WardServiceApp {

    public static void main(String[] args) {

            IngestionClient ingestion = new IngestionClient(ServiceConfig.ingestionUrl());

            List<Ward> wards;
            try {
                wards = ingestion.fetchWards();
            } catch (IllegalStateException error) {
                System.err.println("Cannot start ward-service: " + error.getMessage());
                System.exit(1);
                return;
            }

            System.out.println("Loaded " + wards.size() + " wards from ingestion-service");
            createApp(new WardService(wards)).start(7031);

    }

    public static Javalin createApp(WardService service) {
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

        return app;
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
// MQ TODO: publishes to ActiveMQ queue MqConfig.QUEUE when it detects an equipment failure on one of its wards.
