package nsu.ccfit.ru.trushkov.game.model;

public class StateOrder {
    private StateOrder() {
        throw new IllegalStateException ("utility class");
    }

    private static int seqStateOrder = 1;

    public static int getStateOrder() { return seqStateOrder++; }
}
