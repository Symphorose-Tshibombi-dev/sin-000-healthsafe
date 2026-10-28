package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.exceptions.DownstreamUnavailableException;
import co.wethinkcode.healthsafe.exceptions.StaffingEventPublisher;
import co.wethinkcode.healthsafe.exceptions.WardNotFoundException;
import co.wethinkcode.healthsafe.mq.MqConfig;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Map;

public class StaffingServiceApp {

    public static void main(String[] args) {
        WardClient wards = new WardClient(ServiceConfig.wardUrl());
        AlertLevelClient alertLevels = new AlertLevelClient(ServiceConfig.alertLevelUrl());

        StaffingEventPublisher events = new JmsStaffingEventPublisher(
                new ActiveMQConnectionFactory(MqConfig.BROKER_URL), MqConfig.TOPIC);

        // no calls at startup: staffing-service starts even when the others (or the broker) are down
        createApp(new StaffingService(wards, alertLevels, events)).start(7033);
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

// MQ: publishes a changed schedule to the ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL
// (see StaffingService.broadcastIfChanged and JmsStaffingEventPublisher). ward-service subscribes.