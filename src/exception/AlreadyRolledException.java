package exception;

public class AlreadyRolledException extends RuntimeException {
    public AlreadyRolledException(String message) {
        super(message);
    }
}
