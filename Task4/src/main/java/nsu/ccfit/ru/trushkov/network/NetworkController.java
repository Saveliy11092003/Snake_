package nsu.ccfit.ru.trushkov.network;

import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.game.controller.GameController;
import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;
import nsu.ccfit.ru.trushkov.network.model.message.*;
import nsu.ccfit.ru.trushkov.network.model.thread.*;
import nsu.ccfit.ru.trushkov.observer.*;
import nsu.ccfit.ru.trushkov.observer.context.*;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;

import static nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto.NodeRole.MASTER;

@Slf4j
public class NetworkController implements ObserverNetwork  {

    private final MulticastService multicastService;

    private final ServiceUDP serviceUDP;

    private final NetworkStorage networkStorage = new NetworkStorage();

    private final GameController gameController;

    private Thread threadPing;

    private Thread threadPlayerScheduler;

    public NetworkController(InetAddress ip, int port, GameController gameController) throws IOException {
        this.multicastService = new MulticastService(new HostNetworkKey(ip, port), networkStorage);
        this.gameController = gameController;
        serviceUDP = new ServiceUDP(networkStorage, gameController);
        gameController.registrationNetworkController(this);
    }

    public void startMulticastSender(SnakesProto.GameMessage.AnnouncementMsg message) {
        log.info("start multicast sender");
        gameController.subscriptionOnPlayerManager(this);
        multicastService.sender(message);
    }

    public void startMulticastReceiver() {
        log.info("start multicast receiver");
        this.multicastService.receiver();
    }


    public void startCheckerPlayer() {
        log.info("start checker player");
        this.multicastService.checkerPlayers();
    }

    public void startSenderUDP() {
        log.info("start sender UDP");
        this.serviceUDP.startSender();
    }

    public void startReceiverUDP() {
        log.info("start receive UDP");
        this.serviceUDP.startReceiver();
    }

    public void startCheckerMsgACK() {
        this.serviceUDP.startCheckerMsgACK();
    }

    public void removeMaster() {
        this.networkStorage.removePlayer(this.networkStorage.getMainRole().getKeyMaster());
    }

    @Override
    public void updateNetworkMsg(ContextMainNodeInfo context) {
        this.multicastService.updateAnnouncementMsg(context.getIp(),
                                                    context.getPort(),
                                                    context.getMessage());
    }

    public void subscriptionOnMulticastService(ObserverGameState observerGameState) {
        this.multicastService.addObserverGameState(observerGameState);
    }

    public void addMessageToSend(String nameGame, SnakesProto.GameMessage gameMessage) {
        this.networkStorage.addMessageToSend(new Message(networkStorage.getMasterNetworkByNameGame(nameGame),
                                                         gameMessage));
    }

    public void addMessageToSend(HostNetworkKey key,SnakesProto.GameMessage gameMessage) {
        this.networkStorage.addMessageToSend(new Message(key, gameMessage));
    }

    public void addRoleSelf(SnakesProto.NodeRole role) {
        this.networkStorage.getMainRole().setRoleSelf(role);
    }

    public void synchronizeMsgSeq() {
        Message.updateSeqMsg(this.networkStorage.getLastStateMsgNum());
    }

    public void updateKeyMaster(HostNetworkKey keyMaster, HostNetworkKey keyDeputy) {
        this.networkStorage.addNewUser(keyMaster, new NodeRole(MASTER));
        this.networkStorage.getMainRole().updateKeys(keyMaster, keyDeputy);
    }

    public HostNetworkKey getHostMasterByGame(String nameGame) {
        return this.networkStorage.getMasterNetworkByNameGame(nameGame);
    }

    public void startMasterScheduler(int delay) {
        Thread thread = new Thread(new MasterScheduler (this.networkStorage, delay, gameController));
        thread.start();
    }

    public void startPlayerSchedulers(int delay) {
        threadPing = new Thread(new PingSender(this.networkStorage, delay));
        threadPlayerScheduler = new Thread(new PlayerScheduler(delay, this.networkStorage, this.gameController));
        threadPing.start();
        threadPlayerScheduler.start();
    }

    public void closePlayerSchedulers() {
        log.info("close player schedulers");
        Objects.requireNonNull(this.threadPing, "threadPing require non null");
        Objects.requireNonNull(this.threadPlayerScheduler, "threadPlayerScheduler require non null");
        this.threadPing.interrupt();
        this.threadPlayerScheduler.interrupt();
    }
}