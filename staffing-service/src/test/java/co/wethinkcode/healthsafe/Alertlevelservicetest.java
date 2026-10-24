package co.wethinkcode.healthsafe;

public class Alertlevelservicetest {

    @Test
    void shouldStartAtLevelZero() {
        assertEquals(0, new AlertLevelService().getLevel());
    }

    @Test
    void shouldStoreAValidLevel() {
        AlertLevelService service = new AlertLevelService();
    }

    @Test
    void shouldRejectLevelsOutsideZeroToEight() {
        AlertLevelService service = new AlertLevelService();

        assertThrows(IllegalArgumentException.class, () -> service.setLevel(9));
        assertThrows(IllegalArgumentException.class, () -> service.setLevel(-1));
    }