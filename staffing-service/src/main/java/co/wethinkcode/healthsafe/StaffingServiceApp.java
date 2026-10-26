package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import java.util.Map;

public class StaffingServiceApp {

    public static void main(String[] args) {
        WardClient wards = new WardClient(ServiceConfig.wardUrl());
        AlertLevelClient alertLevels = new AlertLevelClient(ServiceConfig.alertLevelUrl());

        // no calls at startup: staffing-service starts even when the others are down
        createApp(new StaffingService(wards, alertLevels)).start(7033);
    }

    public static Javalin createApp(StaffingService service) {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/schedule/{wardId}", ctx -> {
            String wardId = ctx.pathParam("wardId");
            try {
                ctx.json(service.scheduleFor(wardId));
            } catch (WardNotFoundException error) {
                ctx.status(404).json(Map.of("error", error.getMessage()));
            } catch (DownstreamUnavailableException error) {
                ctx.status(503).json(Map.of("error", error.getMessage()));
            }
        });

        return app;
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)