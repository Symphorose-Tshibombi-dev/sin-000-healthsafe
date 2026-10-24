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

