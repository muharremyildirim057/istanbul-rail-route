package project.istanbulrailroute.domain.exception.passengerException;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(String message){
        super(message);
    }
}
