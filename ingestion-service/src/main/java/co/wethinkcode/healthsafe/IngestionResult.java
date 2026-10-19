package co.wethinkcode.healthsafe;

import java.util.List;

public record IngestionResult(List<Ward> wards, List<String> issues) {
}
