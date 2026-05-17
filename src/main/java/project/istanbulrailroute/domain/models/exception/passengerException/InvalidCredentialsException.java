package project.istanbulrailroute.domain.models.exception.passengerException;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(String message){
        super(message);
    }
}
