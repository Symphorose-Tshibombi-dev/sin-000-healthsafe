package co.wethinkcode.healthsafe;

public class Staffingservicetest {

    @Test
    void shouldGiveOneDoctorAtNormalLevels() {
        Schedule schedule = serviceFor("Cardiology", 1).scheduleFor("W-01");

        assertEquals("W-01", schedule.wardId());
        assertEquals("Cardiology", schedule.department());
        assertEquals(1, schedule.alertLevel());
        assertEquals("NORMAL", schedule.mode());
        assertEquals(1, schedule.doctorsOnCall());
    }
}

@Test
void shouldCallAllHandsAtCodeBlue() {
    Schedule schedule = serviceFor("Cardiology", 8).scheduleFor("W-01");

    assertEquals("CODE BLUE", schedule.mode());
    assertEquals(4, schedule.doctorsOnCall());
}

@Test
void shouldGiveIcuOneExtraDoctor() {
    assertEquals(2, serviceFor("ICU", 0).scheduleFor("W-09").doctorsOnCall());
    assertEquals(5, serviceFor("ICU", 8).scheduleFor("W-09").doctorsOnCall());
}

@Test
void shouldPassOnWardNotFoundAndNotReadTheAlertLevel() {
    StaffingService service = new StaffingService(
            id -> {
                throw new WardNotFoundException(id);
            },
            () -> {
                throw new AssertionError("the alert level must not be read for an unknown ward");
            });

    assertThrows(WardNotFoundException.class, () -> service.scheduleFor("W-99"));
}

@Test
void shouldPassOnAnAlertLevelFailure() {
    StaffingService service = new StaffingService(
            id -> new WardInfo(id, "East Wing", "Cardiology"),
            () -> {
                throw new DownstreamUnavailableException("alert-level-service is down");
            });

    assertThrows(DownstreamUnavailableException.class, () -> service.scheduleFor("W-01"));
}