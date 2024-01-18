package nsu.ccfit.ru.trushkov.game.controller.impl;

import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

import nsu.ccfit.ru.trushkov.game.controller.*;
import nsu.ccfit.ru.trushkov.game.gui.GUIGameSpace;
import nsu.ccfit.ru.trushkov.game.gui.imp.GUIGameMenuImpl;
import nsu.ccfit.ru.trushkov.game.gui.imp.GUIGameSpaceImpl;
import nsu.ccfit.ru.trushkov.observer.context.*;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.util.*;

import static nsu.ccfit.ru.trushkov.context.ContextValue.*;

@Slf4j
public class GUIMenuControllerImpl implements GUIMenuController {
    @FXML
    private TextField nameGame;

    @FXML
    private TextField namePlayer;

    @FXML
    private TextField width;

    @FXML
    private TextField height;

    @FXML
    private TextField countFood;

    @FXML
    private TextField delay;

    @FXML
    private ListView<String> gamesInfo;

    private GameControllerImpl gameController;

    public static final int OPEN_JOIN_WINDOW = 2;

    private GUIGameMenuImpl guiGameMenu;

    private ContextGameState contextGames;

    @Override
    public void dependencyInjection(GameControllerImpl gameController, GUIGameMenuImpl guiGameMenu) {
        log.info("registration game controller");
        this.gameController = gameController;
        this.guiGameMenu = guiGameMenu;
        this.gameController.subscriptionOnMulticastService(this);
    }

    @Override
    public void createGame() {
        log.info("create config {}", nameGame.getText());

        Objects.requireNonNull(gameController, "gameController cannot be null");
        this.launchGUIGameSpace();
        gameController.createConfigGame(nameGame.getText(),
                                        namePlayer.getText(),
                                        SnakesProto.GameConfig.newBuilder()
                                                            .setHeight(Integer.parseInt(height.getText()))
                                                            .setWidth(Integer.parseInt(width.getText()))
                                                            .setStateDelayMs(Integer.parseInt(delay.getText()))
                                                            .setFoodStatic(Integer.parseInt(countFood.getText()))
                                                            .build());
    }

    private void launchGUIGameSpace() {
        GUIGameSpace guiGameSpace = new GUIGameSpaceImpl(nameGame.getText(), guiGameMenu.getStageMenu(), this.gameController);
        guiGameSpace.view();
    }

    @Override
    public void updateGameState(ContextGameState context) {
        this.contextGames = context;
        Platform.runLater(() -> gamesInfo.getItems().setAll(context.getGameAnnouncements().stream()
                                    .map(game->String.format("%s %s %s %s %d", game.getGameName(),
                                                                               SPACE_STR.repeat(SPACE_BETWEEN_WORDS - game.getGameName().length()),
                                                                               game.getCanJoin(),
                                                                               SPACE_STR.repeat(SPACE_BETWEEN_WORDS),
                                                                               game.getPlayers().getPlayersList().size())).toList()));
    }

    private void openJoinWindow(String nameGame) {
        Objects.requireNonNull(this.guiGameMenu, "guiGameMenu required not null");
        Objects.requireNonNull(this.contextGames, "contextGames required not null");
        
        this.launchGUIGameSpace();

        this.contextGames.getGameAnnouncements().stream()
                                                .takeWhile(announcementMsg -> announcementMsg.getGameName().equals(nameGame))
                                                .findFirst()
                                                .ifPresent(this.guiGameMenu::openJoinWindow);
    }

    public void joinToGame(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() != OPEN_JOIN_WINDOW) return;

        gamesInfo.setOnMouseClicked(event -> {
            String selectedItem = gamesInfo.getSelectionModel().getSelectedItem();
            if (selectedItem != null)
                this.openJoinWindow(selectedItem.substring(0, selectedItem.indexOf(SPACE_CHAR)));
        });
    }
}