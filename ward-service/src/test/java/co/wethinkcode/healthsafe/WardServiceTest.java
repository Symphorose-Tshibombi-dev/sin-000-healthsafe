package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class WardServiceTest {

    @Test

    void mustFindWardById(){
        WardService findingService = new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null),
                new Ward("W-04", "North Wing", "Oncology", null, "flagged")
        ));

        Optional<Ward> ward = findingService.findById("W-01");

        assertTrue(ward.isPresent());
        assertEquals("East Wing", ward.get().wardName());
    }

    @Test

    void mustReturnEmptyWhenWardDoesNotExist(){
        WardService findingService = new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null)
        ));

        Optional<Ward> ward = findingService.findById("W-99");
        assertTrue(ward.isEmpty());
    }

    @Test

    void mustReturnAllWards(){
        WardService findingService = new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null),
                new Ward("W-04", "North Wing", "Oncology", null, "flagged")
        ));

        List<Ward> ward = findingService.findAll();

        assertEquals(3, all.size());
        assertEquals("W-01", all.get(0).wardId());
    }
}
