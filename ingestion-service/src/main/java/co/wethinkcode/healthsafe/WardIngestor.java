package co.wethinkcode.healthsafe;

import java.util.*;

public class WardIngestor {
    private static final int EXPECTED_COLUMNS = 4;
    private static final int FIRST_DATA_LINE = 2;

    public static IngestionResult ingest(List<String[]> rawRows){
        Map<String, Ward> wardsById = new LinkedHashMap<>();
        Map<String, Integer> keptLine =new HashMap<>();
        List<String> issues = new ArrayList<>();


        for(int i = 0; i < rawRows.size(); i++){
            int line = i + FIRST_DATA_LINE;
            String[] row = rawRows.get(i);

            if (row.length != EXPECTED_COLUMNS){
                issues.add("Line " + line + ": expected " + EXPECTED_COLUMNS
                                + " columns but found " + row.length + "- row skipped");
                continue;
            }

            Ward ward;

            try{
                ward = toWard(row);
            }catch (IllegalArgumentException exception){
                issues.add("Line " + line + ": " + exception.getMessage() + " - row skipped");
                continue;
            }

            addWard(ward, line, wardsById, keptLine, issues);
        }
        return  new IngestionResult(new ArrayList<>(wardsById.values()),issues);
    }

    private static void addWard(Ward ward, int line, Map<String, Ward> wardsById,
                                Map<String, Integer> keptLine, List<String> issues) {
        String id = ward.wardId();
        Ward existing = wardsById.get(id);

        if (existing == null) {
            wardsById.put(id, ward);
            keptLine.put(id, line);
        } else if (existing.hasNotes() && !ward.hasNotes()) {
            issues.add("Line " + keptLine.get(id) + ": duplicate of " + id
                    + " replaced by the cleaner record on line " + line);
            wardsById.put(id, ward);
            keptLine.put(id, line);
        } else {
            issues.add("Line " + line + ": duplicate of " + id
                    + " (already kept from line " + keptLine.get(id) + ") - row skipped");
        }
    }

    private static Ward toWard(String[] row) {
        String wardId = WardIdCleaner.clean(row[0]);
        String wing = WingNameCleaner.clean(row[1]);
        String department = DepartmentCleaner.clean(row[2]);

        List<String> notes = new ArrayList<>();
        if (wing == null) {
            notes.add("wing missing");
        }
        if (department == null) {
            notes.add("department missing");
        }

        Integer bedsAvailable = cleanBeds(row[3], notes);

        String joinedNotes = notes.isEmpty() ? null : String.join("; ", notes);
        return new Ward(wardId, wing, department, bedsAvailable, joinedNotes);
    }

    private static Integer cleanBeds(String rawBeds, List<String> notes) {
        try {
            return BedAvailabilityCleaner.clean(rawBeds);
        } catch (IllegalArgumentException exception) {
            notes.add("bedsAvailable '" + rawBeds.trim() + "' was invalid - flagged for follow-up");
            return null;
        }
    }
}
