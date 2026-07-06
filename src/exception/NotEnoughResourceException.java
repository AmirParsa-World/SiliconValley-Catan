package exception;

public class NotEnoughResourceException extends RuntimeException {

    public NotEnoughResourceException(String message) {
        super(message);
    }
}
