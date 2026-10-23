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
}
