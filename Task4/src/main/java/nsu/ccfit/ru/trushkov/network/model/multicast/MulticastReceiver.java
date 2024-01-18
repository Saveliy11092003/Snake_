package nsu.ccfit.ru.trushkov.network.model.multicast;

import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.ecxeption.*;
import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;
import nsu.ccfit.ru.trushkov.network.model.message.MainNodeInfo;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;

import java.util.*;
import java.util.concurrent.*;

import static nsu.ccfit.ru.trushkov.context.ContextValue.*;
import static nsu.ccfit.ru.trushkov.network.MulticastService.INDEX_ANNOUNCEMENT_MSG;

@Slf4j
public class MulticastReceiver extends MulticastUDP {

    private final InetAddress multicastGroup;

    private final MulticastSocket socket;

    private final ConcurrentMap<String, MainNodeInfo> mainNodesInfo;

    public MulticastReceiver(InetAddress ip, int port, ConcurrentMap<String, MainNodeInfo> mainNodesInfo) throws IOException {
        super(ip, port);

        this.mainNodesInfo = mainNodesInfo;
        this.socket = new MulticastSocket(port);
        this.multicastGroup = ip;
    }

    public List<SnakesProto.GameMessage.AnnouncementMsg> getListGames() {
        return mainNodesInfo.values().stream().map(MainNodeInfo::getMessage).toList();
    }

    public void addToGroup() {
        try {
            this.socket.joinGroup(new InetSocketAddress(this.ip, this.port), null);
            log.info("create multicast group by port: " + this.port);
        } catch (IOException ex) {
            log.error("could not be added to the group by port {}, ip {}", this.port, this.ip);
            throw new JoinGroupException(Arrays.toString(multicastGroup.getAddress()), this.port);
        }
    }

    private boolean isHostAFK(Date date) {
        return System.currentTimeMillis() - date.getTime() >= MAX_AFK_TIME;
    }

    public void checkAlive() {
        for(Map.Entry<String, MainNodeInfo> liveHost : mainNodesInfo.entrySet()) {
            if(isHostAFK(liveHost.getValue().getDate())) {
                log.info("remove host ip " + liveHost.getKey());
                mainNodesInfo.remove(liveHost.getKey());
            }
        }
    }

    public void receiver() throws ReceiveDatagramException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            this.socket.receive(packet);
            byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(data);
            this.putAnnouncementMsgByIp(message.getAnnouncement().getGames(INDEX_ANNOUNCEMENT_MSG).getGameName(),
                                        packet.getAddress(),
                                        packet.getPort(),
                                        message.getAnnouncement());
        } catch (IOException e) {
            log.error("socket by port {} : {}", socket.getLocalPort(), e.getMessage());
        }
    }

    public void putAnnouncementMsgByIp(String nameGame, InetAddress ip, int port,
                                       SnakesProto.GameMessage.AnnouncementMsg announcementMsg) {
        mainNodesInfo.put(nameGame, new MainNodeInfo(new HostNetworkKey(ip, port),
                                                     announcementMsg,
                                                     new Date(System.currentTimeMillis())));
    }

    public void leaveGroup() {
        try {
            this.socket.leaveGroup(new InetSocketAddress(this.multicastGroup, this.port), null);
            this.socket.close();
        } catch(IOException ex) {
            log.error("failed to exit from multicast group, ex {}", ex.getMessage());
        }
    }
}
