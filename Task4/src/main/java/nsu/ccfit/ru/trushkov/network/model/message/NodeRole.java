package nsu.ccfit.ru.trushkov.network.model.message;


import lombok.Getter;
import lombok.Setter;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

@Getter
public class NodeRole {
    @Setter
    private SnakesProto.NodeRole role;

    private long currTime = System.currentTimeMillis();

    public  NodeRole(SnakesProto.NodeRole role) {
        this.role = role;
    }

    public void updateTime() {
        this.currTime = System.currentTimeMillis();
    }
}
