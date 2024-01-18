package nsu.ccfit.ru.trushkov.game.controller;

import javafx.fxml.Initializable;
import nsu.ccfit.ru.trushkov.game.gui.GUIGameMenu;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

public interface GUIJoinController extends Initializable {
    void joinGame();

    void dependencyInjection(GameController gameController, GUIGameMenu guiGameMenu);

    void errorJoinToGame(String message);

    void intiGameState(SnakesProto.GameAnnouncement gameState);
}
