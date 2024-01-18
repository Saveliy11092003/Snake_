package nsu.ccfit.ru.trushkov.ecxeption;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException() {
        super("player not found by role");
    }
}
