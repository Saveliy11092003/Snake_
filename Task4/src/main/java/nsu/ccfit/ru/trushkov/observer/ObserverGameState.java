package nsu.ccfit.ru.trushkov.observer;

import nsu.ccfit.ru.trushkov.observer.context.ContextGameState;

public interface ObserverGameState {
    void updateGameState(ContextGameState context);
}
