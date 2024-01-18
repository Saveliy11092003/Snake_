package nsu.ccfit.ru.trushkov.ecxeption;

public class ReceiveDatagramException extends RuntimeException {
    public ReceiveDatagramException(String message) {
            super("receiver exception " + message);
    }
}
