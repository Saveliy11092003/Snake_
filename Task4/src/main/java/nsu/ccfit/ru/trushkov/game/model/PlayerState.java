package nsu.ccfit.ru.trushkov.game.model;

import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

public record PlayerState(Integer playerID, String playerName, String nameGame, SnakesProto.NodeRole role) {}