package nsu.ccfit.ru.trushkov.network.model.gamemessage;

import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

public class ChangeMsg {

    private ChangeMsg() {
        throw new IllegalStateException("utility class");
    }

    public static SnakesProto.GameMessage.RoleChangeMsg create(SnakesProto.NodeRole role) {
        return SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                                                    .setReceiverRole(role)
                                                    .build();
    }
}
