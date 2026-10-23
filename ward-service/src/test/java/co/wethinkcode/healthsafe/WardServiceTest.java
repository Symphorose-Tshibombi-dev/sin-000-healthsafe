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
        assertEquals("East Wing", ward.get().wing());
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

        List<Ward> all = findingService.findAll();

        assertEquals(2, all.size());
        assertEquals("W-01", all.get(0).wardId());
    }

    @Test
    void mustReturnEachDepartmentOnce() {
        WardService findingService = new WardService(List.of(
                new Ward("W-01", "East Wing", "Cardiology", 3, null),
                new Ward("W-02", "West Wing", "Cardiology", 5, null),
                new Ward("W-04", "North Wing", "Oncology", null, "flagged")
        ));

        List<String> departments = findingService.findDepartments();

        assertEquals(2, departments.size());
        assertTrue(departments.contains("Cardiology"));
        assertTrue(departments.contains("Oncology"));
    }
}
