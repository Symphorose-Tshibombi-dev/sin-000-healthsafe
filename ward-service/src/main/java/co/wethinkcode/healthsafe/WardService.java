package co.wethinkcode.healthsafe;

import java.util.List;
import java.util.Optional;

public class WardService {

    private final List<Ward> wards;

    public WardService(List<Ward> wards){
        this.wards = wards;
    }

    public Optional<Ward> findById(String id){
        return Optional.empty();
    }
}
