package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.exceptions.AlertLevelSource;
import co.wethinkcode.healthsafe.exceptions.StaffingEventPublisher;
import co.wethinkcode.healthsafe.exceptions.WardLookup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Works out how many doctors must be on call for a ward.
 *
 * Assumptions (ours, the README does not fix the rule):
 *  - the higher the Emergency Status, the more doctors:
 *    levels 0-2 -> 1, levels 3-5 -> 2, levels 6-7 -> 3, level 8 (Code Blue) -> 4
 *  - an ICU ward always gets one extra doctor
 *  - a schedule is broadcast on the topic only when it differs from the last one
 *    broadcast for that ward, so repeated reads do not spam subscribers
 */
public class StaffingService {

    private final WardLookup wards;
    private final AlertLevelSource alertLevels;
    private final StaffingEventPublisher events;
    private final Map<String, Schedule> lastBroadcast = new ConcurrentHashMap<>();

    /** Without a publisher: schedules are computed but never broadcast. */
    public StaffingService(WardLookup wards, AlertLevelSource alertLevels) {
        this(wards, alertLevels, schedule -> {
        });
    }

    public StaffingService(WardLookup wards, AlertLevelSource alertLevels, StaffingEventPublisher events) {
        this.wards = wards;
        this.alertLevels = alertLevels;
        this.events = events;
    }

    public Schedule scheduleFor(String wardId) {
        // ward first: if it does not exist there is no point reading the alert level
        WardInfo ward = wards.findWard(wardId);
        int level = alertLevels.currentLevel();

        Schedule schedule = new Schedule(ward.wardId(), ward.department(), level, modeFor(level), doctorsFor(level, ward.department()));
        broadcastIfChanged(schedule);
        return schedule;
    }

    /** Best effort: the caller still gets the schedule when the broker is down, and we retry next time. */
    private void broadcastIfChanged(Schedule schedule) {
        Schedule previous = lastBroadcast.put(schedule.wardId(), schedule);
        if (schedule.equals(previous)) {
            return;
        }
        try {
            events.publish(schedule);
        } catch (RuntimeException error) {
            lastBroadcast.remove(schedule.wardId());
            System.err.println("Staffing event for " + schedule.wardId() + " could not be sent: " + error.getMessage());
        }
    }

    private static String modeFor(int level) {
        if (level <= 2) {
            return "NORMAL";
        }
        if (level <= 5) {
            return "ELEVATED";
        }
        if (level <= 7) {
            return "HIGH";
        }
        return "CODE BLUE";
    }

    private static int doctorsFor(int level, String department) {
        int doctors;
        if (level <= 2) {
            doctors = 1;
        } else if (level <= 5) {
            doctors = 2;
        } else if (level <= 7) {
            doctors = 3;
        } else {
            doctors = 4;
        }

        if ("ICU".equalsIgnoreCase(department)) {
            doctors++;
        }
        return doctors;
    }
}