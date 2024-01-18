package nsu.ccfit.ru.trushkov.game.gui;

import nsu.ccfit.ru.trushkov.observer.context.ContextGame;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

public interface GUIGameSpace extends View {
    void drawBackground();
    void drawSnake(SnakesProto.GameState.Snake snake);
    void update(ContextGame context);

    void printErrorMessage(String message);
}
