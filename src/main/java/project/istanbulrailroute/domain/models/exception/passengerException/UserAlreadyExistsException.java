package project.istanbulrailroute.domain.models.exception.passengerException;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String message){
        super(message);
    }
}
