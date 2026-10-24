package co.wethinkcode.healthsafe;

public class Alertlevelservicetest {

    @Test
    void shouldStartAtLevelZero() {
        assertEquals(0, new AlertLevelService().getLevel());
    }
}
