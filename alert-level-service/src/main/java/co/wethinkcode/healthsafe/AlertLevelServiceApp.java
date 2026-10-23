package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import java.util.Map;

public class AlertLevelServiceApp {

    public record LevelRequest(Integer level) {
    }

    public static void main(String[] args) {
        createApp(new AlertLevelService()).start(7032);
    }

    public static Javalin createApp(AlertLevelService service) {
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
                service.setLevel(request.level());
                ctx.json(Map.of("level", service.getLevel()));
            } catch (IllegalArgumentException error) {
                ctx.status(400).json(Map.of("error", error.getMessage()));
            }
        });

        return app;
    }
        // TODO (Tracks the hospital Emergency Status (0-8, 8 = full Code Blue).)
        // Add domain endpoints for alert-level-service here.
}
