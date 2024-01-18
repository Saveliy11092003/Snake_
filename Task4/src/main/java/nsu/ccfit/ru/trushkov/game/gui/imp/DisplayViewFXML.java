package nsu.ccfit.ru.trushkov.game.gui.imp;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.game.gui.GUIGameMenu;

@Slf4j
public abstract class DisplayViewFXML implements GUIGameMenu {

    public void view(Stage stage, Pane root) {
        log.info("start view menu");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
