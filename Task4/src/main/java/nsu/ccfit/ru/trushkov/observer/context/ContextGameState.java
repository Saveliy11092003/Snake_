package nsu.ccfit.ru.trushkov.observer.context;

import lombok.Getter;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.util.List;

import static nsu.ccfit.ru.trushkov.context.ContextValue.DEFAULT_NUMBER_GAME_MSG;

@Getter
public class ContextGameState implements Context {

    private List<SnakesProto.GameAnnouncement> gameAnnouncements;

    public void updateGameState(List<SnakesProto.GameMessage.AnnouncementMsg> games) {
        this.gameAnnouncements = games.stream().map(game->game.getGames(DEFAULT_NUMBER_GAME_MSG)).toList();
    }
}