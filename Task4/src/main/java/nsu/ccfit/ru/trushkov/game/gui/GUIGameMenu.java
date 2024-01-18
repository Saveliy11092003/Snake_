package nsu.ccfit.ru.trushkov.game.gui;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

public interface GUIGameMenu extends View {
    void openJoinWindow(SnakesProto.GameAnnouncement gameState);

    void cancelJoinWindow();

    void view(Stage stage, Pane pane);
}