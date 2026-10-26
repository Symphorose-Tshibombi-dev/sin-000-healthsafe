package co.wethinkcode.healthsafe.exceptions;

public class WardNotFoundException extends RuntimeException {

    public WardNotFoundException(String wardId) {
        super("Ward " + wardId + " not found");
    }
}