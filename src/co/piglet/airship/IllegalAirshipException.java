package co.piglet.airship;

/**
 * Thrown when the creation of an airship fails.
 * This can currently only be thrown if the number of blocks exceeds the limit
 *
 * @author Ben Carvell
 */
public class IllegalAirshipException extends Throwable {
    public IllegalAirshipException(String message) {
        super(message);
    }
}

