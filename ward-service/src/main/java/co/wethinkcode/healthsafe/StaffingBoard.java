package co.wethinkcode.healthsafe;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** The latest staffing update received for each ward (in memory, resets on restart). */
public class StaffingBoard {

    private final Map<String, StaffingUpdate> latest = new ConcurrentHashMap<>();

    public void record(StaffingUpdate update) {
        latest.put(update.wardId(), update);
    }

    public Optional<StaffingUpdate> latestFor(String wardId) {
        return Optional.ofNullable(latest.get(wardId));
    }
}