package co.wethinkcode.healthsafe;

import java.util.List;
import java.util.Optional;

public class WardService {

    private final List<Ward> wards;

    public WardService(List<Ward> wards){
        this.wards = wards;
    }

    public Optional<Ward> findById(String id){
        for(Ward ward : wards){
            if (ward.wardId().equals(id)){
                return  Optional.of(ward);
            }
        }
        return Optional.empty();
    }

    public List<Ward> findAll(){
        return wards;
    }

    public List<String> findDepartments(){
        return wards.stream()
                .map(Ward::department)
                .distinct()
                .toList();
    }
}
