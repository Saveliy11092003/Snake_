package nsu.ccfit.ru.trushkov.game.controller;

import nsu.ccfit.ru.trushkov.game.controller.impl.GameControllerImpl;
import nsu.ccfit.ru.trushkov.game.gui.imp.GUIGameMenuImpl;
import nsu.ccfit.ru.trushkov.observer.ObserverGameState;

public interface GUIMenuController extends ObserverGameState {

    void createGame();

    void dependencyInjection(GameControllerImpl gameController, GUIGameMenuImpl guiGameMenu);
}