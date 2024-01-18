package nsu.ccfit.ru.trushkov.ecxeption;

public class FindSuitableSquareException extends RuntimeException {
    public FindSuitableSquareException() {
        super("couldn't find a suitable square");
    }
}
