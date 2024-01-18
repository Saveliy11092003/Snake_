package nsu.ccfit.ru.trushkov.ecxeption;

public class ClassLoaderException extends RuntimeException {
    public ClassLoaderException(String message) {
        super("failed to load xml at path " + message);
    }
}
