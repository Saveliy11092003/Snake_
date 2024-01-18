package nsu.ccfit.ru.trushkov.observer;

import nsu.ccfit.ru.trushkov.observer.context.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Observable {

    private List<ObserverNetwork> observerNetworks = null;

    private List<ObserverGameState> observerGameStates = null;

    private List<ObserverState> observerState = null;

    private List<ObserverError> observerErrors = null;


    public void addObserverState(ObserverState observer) {
        if (observerState == null) observerState = new CopyOnWriteArrayList<>();
        observerState.add(observer);
    }

    public void addObserverGameState(ObserverGameState observerGameState) {
        if (observerGameStates == null) observerGameStates = new CopyOnWriteArrayList<>();
        observerGameStates.add(observerGameState);
    }

    public void addObserverError(ObserverError observer) {
        if (observerErrors == null) observerErrors = new CopyOnWriteArrayList<>();
        observerErrors.add(observer);
    }

    public void addObserverNetwork(ObserverNetwork observer) {
        if (observerNetworks == null) observerNetworks = new CopyOnWriteArrayList<>();
        observerNetworks.add(observer);
    }

    public void notifyObserversGameState() {
        Objects.requireNonNull(observerState, "observerState can't be null");
        for (ObserverState observer : observerState) {
            observer.updateState();
        }
    }

    public void notifyObserversError(ContextError context) {
        Objects.requireNonNull(observerErrors, "observerErrors can't be null");
        for (ObserverError observer : observerErrors) {
            observer.updateError(context);
        }
    }

    public void notifyObserversNetwork(ContextMainNodeInfo context) {
        Objects.requireNonNull(observerNetworks, "observers can't be null");
        for (ObserverNetwork observer : observerNetworks) {
            observer.updateNetworkMsg(context);
        }
    }

    public void notifyObserversGameState(ContextGameState context) {
        Objects.requireNonNull(observerGameStates, "observers can't be null");
        for (ObserverGameState observer : observerGameStates) {
            observer.updateGameState(context);
        }
    }
}
