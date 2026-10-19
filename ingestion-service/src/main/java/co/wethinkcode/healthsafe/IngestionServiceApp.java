package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

public class IngestionServiceApp {

    private static final int PORT = 7030;
    private static final String CSV_FILE = "wards-outdated.csv";

    public static void main(String[] args) throws Exception {
        // 1. Read and clean the CSV once, when the service starts
        IngestionResult result = WardIngestor.ingest(WardCsvReader.readRecords(CSV_FILE));

        // 2. Start the web server on port 7030
        createApp(result).start(PORT);
    }

    public static Javalin createApp(IngestionResult result){
        Javalin app = Javalin.create();

        app.get("/health" , ctx -> ctx.result("OK"));
        app.get("/wards", ctx -> ctx.json(result.wards()));
        app.get("/issues", ctx -> ctx.json(result.issues()));

        return app;
    }

}
