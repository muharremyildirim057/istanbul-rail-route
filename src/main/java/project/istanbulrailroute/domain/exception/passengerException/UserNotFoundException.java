package project.istanbulrailroute.domain.exception.passengerException;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
