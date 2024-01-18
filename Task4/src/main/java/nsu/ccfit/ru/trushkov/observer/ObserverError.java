package nsu.ccfit.ru.trushkov.observer;

import nsu.ccfit.ru.trushkov.observer.context.ContextError;

public interface ObserverError {
    void updateError(ContextError context);
}
