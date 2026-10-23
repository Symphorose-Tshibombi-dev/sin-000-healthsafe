import co.wethinkcode.healthsafe.AlertLevelService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AlertLevelServiceTest {
    @Test
    void shouldStartAtLevelZero() {
        assertEquals(0, new AlertLevelService().getLevel());
    }

    @Test
    void shouldStoreAValidLevel() {
        AlertLevelService service = new AlertLevelService();

        service.setLevel(5);

        assertEquals(5, service.getLevel());
    }

    @Test
    void shouldAcceptBothEdgesOfTheRange() {
        AlertLevelService service = new AlertLevelService();

        service.setLevel(0);
        assertEquals(0, service.getLevel());

        service.setLevel(8);
        assertEquals(8, service.getLevel());
    }

    @Test
    void shouldRejectLevelsOutsideZeroToEight() {
        AlertLevelService service = new AlertLevelService();

        assertThrows(IllegalArgumentException.class, () -> service.setLevel(9));
        assertThrows(IllegalArgumentException.class, () -> service.setLevel(-1));
    }

    @Test
    void shouldKeepTheOldLevelWhenANewOneIsRejected() {
        AlertLevelService service = new AlertLevelService();
        service.setLevel(3);

        assertThrows(IllegalArgumentException.class, () -> service.setLevel(99));

        assertEquals(3, service.getLevel());
    }

}
