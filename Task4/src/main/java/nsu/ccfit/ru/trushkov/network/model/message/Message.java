package nsu.ccfit.ru.trushkov.network.model.message;

import lombok.Getter;
import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.net.DatagramPacket;

@Getter
public final class Message {
    private final DatagramPacket packet;

    private final HostNetworkKey hostNetworkKey;

    private final SnakesProto.GameMessage gameMessage;

    @Getter
    private long timeSent;

    public void updateTimeSent() {
         this.timeSent = System.currentTimeMillis();
    }

    public Message(HostNetworkKey hostNetworkKey, SnakesProto.GameMessage gameMessage) {
        this.packet = new DatagramPacket(gameMessage.toByteArray(),
                                         gameMessage.getSerializedSize(),
                                         hostNetworkKey.getIp(),
                                         hostNetworkKey.getPort());
        this.hostNetworkKey = hostNetworkKey;
        this.gameMessage = gameMessage;
    }

    private static long seqMsg = 0;
    public static void updateSeqMsg(long updateSeqMsg) {
        seqMsg = updateSeqMsg;
    }
    public static long getSeqNumber() { return ++seqMsg; }
}
