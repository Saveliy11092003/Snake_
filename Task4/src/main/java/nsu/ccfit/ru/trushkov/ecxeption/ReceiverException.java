package nsu.ccfit.ru.trushkov.ecxeption;

public class ReceiverException extends RuntimeException {
    public ReceiverException(String message) {
        super("receiver exception " + message);
    }
}
