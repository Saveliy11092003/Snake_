package nsu.ccfit.ru.trushkov.game.controller.impl;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.game.gui.imp.GUIGameMenuImpl;
import nsu.ccfit.ru.trushkov.observer.*;
import nsu.ccfit.ru.trushkov.observer.context.*;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.io.IOException;
import java.util.*;

import static nsu.ccfit.ru.trushkov.context.ContextValue.*;

@Slf4j
public class ControllerGameState implements ObserverGameState, ObserverError {

    @FXML
    private ListView<String> playersState;

    @FXML
    private ListView<String>  gamesState;

    @FXML
    private Label errorJoinGame;

    private String gameName;

    private Stage stage;

    private GameControllerImpl gameController;

    private static final String MASTER_INSTRUCTION = "(MASTER)";

    private static final String DEPUTY_INSTRUCTION = "(DEPUTY)";

    public void updateStateView(String nameGame, Stage stage) {
        this.gameName = nameGame;
        this.stage = stage;
    }
    public void dependencyGameController(GameControllerImpl gameController) {
        this.gameController = gameController;
        gameController.addObserverError(this);
    }

    public String getNameByRole(String namePlayer, SnakesProto.NodeRole role) {
        return switch (role) {
            case MASTER -> namePlayer + MASTER_INSTRUCTION;
            case DEPUTY -> namePlayer + DEPUTY_INSTRUCTION;
            default -> namePlayer;
        };
    }

    @Override
    public void updateGameState(ContextGameState context) {
        Platform.runLater(() -> {
            this.gamesState.getItems().setAll(context.getGameAnnouncements().stream()
                .map(action -> String.format("%s %s %d", action.getGameName(),
                                                         SPACE_STR.repeat(SPACE_BETWEEN_WORDS),
                                                         action.getPlayers().getPlayersList().size()))
                .toList());

            Optional<SnakesProto.GameAnnouncement> gameAnn = context.getGameAnnouncements().stream()
                                                                    .takeWhile(game -> game.getGameName().equals(gameName))
                                                                    .findFirst();

            gameAnn.ifPresent(announcement ->
                this.playersState.getItems().setAll(
                    announcement.getPlayers().getPlayersList().stream()
                        .map(player -> new AbstractMap.SimpleEntry<>(player, player.getScore()))
                        .sorted(Comparator.comparingInt(entry -> -entry.getValue()))
                        .map(entry -> String.format("%s %s %d", getNameByRole(entry.getKey().getName(), entry.getKey().getRole()),
                                                                              SPACE_STR.repeat(SPACE_BETWEEN_WORDS),
                                                                              entry.getValue()))
                    .toList())
            );
        });
    }

    public void logout() throws IOException {
        new GUIGameMenuImpl(gameController, stage).view();
    }

    @Override
    public void updateError(ContextError context) {
        errorJoinGame.setText(context.getMessage());
    }
}