package nsu.ccfit.ru.trushkov.ecxeption;

public class TypeCaseException extends RuntimeException {
    public TypeCaseException() {
        super("no such type case");
    }

    public TypeCaseException(Throwable ex) {
        super("no such type case: " + ex.getMessage());
    }
}
