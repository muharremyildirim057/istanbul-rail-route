package project.istanbulrailroute.domain.exception.virtualCardException;

public class InvalidCardException extends RuntimeException{
    public InvalidCardException(String message){
        super(message);
    }
}
