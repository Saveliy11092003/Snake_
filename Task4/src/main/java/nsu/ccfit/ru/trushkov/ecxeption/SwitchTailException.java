package nsu.ccfit.ru.trushkov.ecxeption;

public class SwitchTailException extends RuntimeException {
    public SwitchTailException() {
        super("failed to find a position for the tail");
    }
}
