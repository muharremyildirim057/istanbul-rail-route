package project.istanbulrailroute.domain.models.exception.virtualCardException;

public class InvalidCardException extends RuntimeException{
    public InvalidCardException(String message){
        super(message);
    }
}
