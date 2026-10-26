package co.wethinkcode.healthsafe;

public class WardNotFoundException extends RuntimeException {

    public WardNotFoundException(String wardId) {
        super("Ward " + wardId + " not found");
    }
}