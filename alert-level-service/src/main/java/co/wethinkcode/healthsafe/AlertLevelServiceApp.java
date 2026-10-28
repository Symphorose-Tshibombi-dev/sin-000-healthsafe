package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.mq.MqConfig;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.time.Instant;
import java.util.Map;

public class AlertLevelServiceApp {

    public record LevelRequest(Integer level) {
    }

    public static void main(String[] args) {
        AlertLevelPublisher publisher = new JmsAlertLevelPublisher(
                new ActiveMQConnectionFactory(MqConfig.BROKER_URL), MqConfig.LEVEL_TOPIC);
        createApp(new AlertLevelService(), publisher).start(7032);
    }

    /** Without a publisher: level changes are simply not announced. */
    public static Javalin createApp(AlertLevelService service) {
        return createApp(service, change -> {
        });
    }

    public static Javalin createApp(AlertLevelService service, AlertLevelPublisher publisher) {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/alert-level", ctx -> ctx.json(Map.of("level", service.getLevel())));

        app.put("/alert-level", ctx -> {
            LevelRequest request;
            try {
                request = ctx.bodyAsClass(LevelRequest.class);
            } catch (Exception error) {
                ctx.status(400).json(Map.of("error", "Body must be JSON like {\"level\": 3}"));
                return;
            }
            if (request.level() == null) {
                ctx.status(400).json(Map.of("error", "Body must contain a level, like {\"level\": 3}"));
                return;
            }

            try {
                int previous = service.getLevel();
                service.setLevel(request.level());
                int current = service.getLevel();

                if (current != previous) {
                    announce(publisher, new AlertLevelChange(previous, current, Instant.now().toString()));
                }
                ctx.json(Map.of("level", current));
            } catch (IllegalArgumentException error) {
                ctx.status(400).json(Map.of("error", error.getMessage()));
            }
        });

        return app;
    }

    /** The change is already stored, so a broker problem must not turn the PUT into an error. */
    private static void announce(AlertLevelPublisher publisher, AlertLevelChange change) {
        try {
            publisher.publish(change);
        } catch (AlertLevelPublisher.AlertLevelPublishException error) {
            System.err.println("Alert level changed but the topic update could not be sent: " + error.getMessage());
        }
    }
}