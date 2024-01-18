package nsu.ccfit.ru.trushkov.network.model.message;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.network.model.keynode.*;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class NetworkStorage {
    @Getter
    private final ConcurrentMap<String, MainNodeInfo> mainNodesInfo = new ConcurrentHashMap<>();

    private final ConcurrentLinkedDeque<Message> messagesToSend = new ConcurrentLinkedDeque<>();

    private final Map<HostNetworkKey, NodeRole> players = new ConcurrentHashMap<>();

    private final Map<Long, NodeInfo> sentMessages = new ConcurrentHashMap<>();

    @Getter
    private final MainRole mainRole = new MainRole();

    @Getter
    private long lastSendTime;

    @Getter
    private long lastStateMsgNum;

    @Getter
    private SnakesProto.GameState currentStateGame;

    public SnakesProto.GameMessage.AnnouncementMsg announcementMsgByNameGame(String nameGame) {
        return mainNodesInfo.get(nameGame).getMessage();
    }

    public boolean isContainsMaster(long delay) {
        return System.currentTimeMillis() - this.players.get(mainRole.getKeyMaster()).getCurrTime() < delay;
    }

    public void updateStateGame(SnakesProto.GameState currentStateGame) {
        this.currentStateGame = currentStateGame;
    }

    public void updateMainRole(HostNetworkKey keyMaster, HostNetworkKey keyDeputy) {
        this.mainRole.updateKeys(keyMaster, keyDeputy);
    }

    public boolean isContainsPlayer(HostNetworkKey key) { return players.containsKey(key); }

    public void updaterDispatchTimePlayer(HostNetworkKey key) {
        this.players.get(key).updateTime();
    }

    public void updateLastStateMsgNum(long seqNum) {
        this.lastStateMsgNum = seqNum;
    }

    public void updateLastSendTime() {
        this.lastSendTime = System.currentTimeMillis();
    }

    public HostNetworkKey getMasterNetworkByNameGame(String nameGame) {
        return this.mainNodesInfo.get(nameGame).getHostNetworkKey();
    }

    public void removePlayer(HostNetworkKey hostNetworkKey) {
        this.players.remove(hostNetworkKey);
    }

    public  Set<Map.Entry<HostNetworkKey, NodeRole>> getSetPlayers() { return players.entrySet(); }

    public void addNewUser(HostNetworkKey hostNetworkKey, NodeRole nodeRole) {
        players.put(hostNetworkKey, nodeRole);
    }

    public void addMessageToSend(Message message) {
        this.messagesToSend.add(message);
    }

    public void addMessageToSendFirst(Message message) {
        this.messagesToSend.addFirst(message);
    }

    public void removeMessageToSend(Message message) {
        this.messagesToSend.remove(message);
    }

    public void addSentMessage(Long key, NodeInfo nodeInfo) {
        sentMessages.put(key, nodeInfo);
    }

    public void removeSentMessage(Long key) {
        sentMessages.remove(key);
    }

    public List<Message> getMessagesToSend() {
        return this.messagesToSend.stream().toList();
    }

    public Set<Map.Entry<Long, NodeInfo>> getEntrySetSentMessage() {
        return this.sentMessages.entrySet();
    }
}
