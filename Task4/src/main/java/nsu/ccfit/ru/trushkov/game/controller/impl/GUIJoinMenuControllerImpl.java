package nsu.ccfit.ru.trushkov.game.controller.impl;

import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import nsu.ccfit.ru.trushkov.game.controller.*;
import nsu.ccfit.ru.trushkov.game.gui.GUIGameMenu;
import nsu.ccfit.ru.trushkov.network.model.gamemessage.GameMessage;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.net.URL;
import java.util.*;

@Slf4j
public class GUIJoinMenuControllerImpl implements GUIJoinController {

    @FXML
    private TextField namePlayer;

    @FXML
    private ChoiceBox<String> choiceRole;

    @FXML
    private Label errorJoinGame;

    private GameController gameController;

    private GUIGameMenu guiGameMenu;

    private String nameGame;

    private SnakesProto.GameConfig gameConfig;

    private static final String VIEWER = "VIEWER";

    private static final String PLAYER = "PLAYER";

    @Override
    public void dependencyInjection(GameController gameController, GUIGameMenu guiGameMenu) {
        this.gameController = gameController;
        this.guiGameMenu = guiGameMenu;
    }

    public void intiGameState(SnakesProto.GameAnnouncement gameState) {
        this.nameGame = gameState.getGameName();
        this.gameConfig = gameState.getConfig();
    }

    @Override
    public void joinGame() {
        log.info("join to game");
        SnakesProto.NodeRole role = this.getRole(choiceRole.getValue());
        Objects.requireNonNull(guiGameMenu, "guiGameMenu require non null");
        guiGameMenu.cancelJoinWindow();

        gameController.initJoinGame(namePlayer.getText(), nameGame, role, this.gameConfig);
        gameController.sendMessageNetwork(nameGame, GameMessage.createGameMessage(nameGame, namePlayer.getText(), role));
    }

    private SnakesProto.NodeRole getRole(String role) {
        return role.equals(VIEWER) ? SnakesProto.NodeRole.VIEWER : SnakesProto.NodeRole.NORMAL;
    }

    @Override
    public void errorJoinToGame(String message) {
        Platform.runLater(() -> errorJoinGame.setText(message));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiceRole.getItems().setAll(VIEWER, PLAYER);
    }
}
