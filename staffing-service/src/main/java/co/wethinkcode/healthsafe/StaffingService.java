package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.exceptions.AlertLevelSource;
import co.wethinkcode.healthsafe.exceptions.SchedulePublisher;
import co.wethinkcode.healthsafe.exceptions.WardLookup;

/**
 * Works out how many doctors must be on call for a ward.
 *
 * Assumptions (ours, the README does not fix the rule):
 *  - the higher the Emergency Status, the more doctors:
 *    levels 0-2 -> 1, levels 3-5 -> 2, levels 6-7 -> 3, level 8 (Code Blue) -> 4
 *  - an ICU ward always gets one extra doctor
 */
public class StaffingService {

    private final WardLookup wards;
    private final AlertLevelSource alertLevels;
    private final SchedulePublisher publisher;

    public StaffingService(WardLookup wards, AlertLevelSource alertLevels) {
        this(wards, alertLevels,schedule -> { });
    }

    public StaffingService(WardLookup wards, AlertLevelSource alertLevels, SchedulePublisher publisher) {
        this.wards = wards;
        this.alertLevels = alertLevels;
        this.publisher = publisher;
    }

    public Schedule scheduleFor(String wardId) {
        // ward first: if it does not exist there is no point reading the alert level
        WardInfo ward = wards.findWard(wardId);
        int level = alertLevels.currentLevel();

        Schedule schedule = new Schedule(ward.wardId(), ward.department(), level, modeFor(level), doctorsFor(level, ward.department()));
        publisher.publish(schedule);
        return schedule;
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