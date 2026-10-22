package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import java.util.Map;
import java.util.Optional;

public class WardServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7031);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Provides lists of wards and departments.)
        // Add domain endpoints for ward-service here.
    }
    public static Javalin createApp(WardService service) {
        Javalin app = Javalin.create();

        app.get("/wards/{id}", ctx -> {
            String id = ctx.pathParam("id");
            Optional<Ward> ward = service.findById(id);

            if (ward.isPresent()) {
                ctx.json(ward.get());
            }else {
                ctx.status(404).json(Map.of("error", "Ward " + id + " not found"));
            }
        });

        return app;
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
// MQ TODO: publishes to ActiveMQ queue MqConfig.QUEUE when it detects an equipment failure on one of its wards.
