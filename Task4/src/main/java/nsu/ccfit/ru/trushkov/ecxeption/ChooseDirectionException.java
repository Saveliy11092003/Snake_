package nsu.ccfit.ru.trushkov.ecxeption;

public class ChooseDirectionException extends RuntimeException {
    public ChooseDirectionException() {
        super("couldn't find the turn of the snake");
    }
}
