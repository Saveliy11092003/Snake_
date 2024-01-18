package nsu.ccfit.ru.trushkov.network;

import lombok.extern.slf4j.Slf4j;

import nsu.ccfit.ru.trushkov.ecxeption.ReceiveDatagramException;

import nsu.ccfit.ru.trushkov.network.model.gamemessage.GameMessage;
import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;
import nsu.ccfit.ru.trushkov.network.model.message.*;
import nsu.ccfit.ru.trushkov.network.model.multicast.*;
import nsu.ccfit.ru.trushkov.observer.Observable;
import nsu.ccfit.ru.trushkov.observer.context.ContextGameState;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import static nsu.ccfit.ru.trushkov.network.model.multicast.MulticastUDP.TIMER_DELAY;

@Slf4j
public class MulticastService extends Observable {
    public static final int INDEX_ANNOUNCEMENT_MSG = 0;

    private final MulticastReceiver multicastReceiver;

    private SnakesProto.GameMessage.AnnouncementMsg message;

    private final ContextGameState context = new ContextGameState();

    private final NetworkStorage networkStorage;

    private final HostNetworkKey hostNetworkKey;

    public MulticastService(HostNetworkKey hostNetworkKey, NetworkStorage networkStorage) throws IOException {
        this.hostNetworkKey = hostNetworkKey;
        this.multicastReceiver = new MulticastReceiver(hostNetworkKey.getIp(),
                                                       hostNetworkKey.getPort(),
                                                       networkStorage.getMainNodesInfo());
        this.multicastReceiver.addToGroup();
        this.networkStorage = networkStorage;
    }

    public void receiver() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    multicastReceiver.receiver();
                    context.updateGameState(multicastReceiver.getListGames());
                    MulticastService.super.notifyObserversGameState(context);
                } catch (ReceiveDatagramException e) {
                    throw new ReceiveDatagramException(e.getMessage());
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, TIMER_DELAY);
    }

    public void sender(SnakesProto.GameMessage.AnnouncementMsg message) {
        this.message = message;
        Objects.requireNonNull(message, "announcementMsg message cannot be null");
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                networkStorage.addMessageToSend(new Message(hostNetworkKey,
                                                GameMessage.createGameMessage(MulticastService.this.message)));
            }
        };
        timer.scheduleAtFixedRate(task, 0, TIMER_DELAY);
    }

    public void checkerPlayers() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                multicastReceiver.checkAlive();
            }
        };
        timer.scheduleAtFixedRate(task, 0, TIMER_DELAY);
    }

    public void updateAnnouncementMsg(InetAddress ip, int port, SnakesProto.GameMessage.AnnouncementMsg message) {
        this.message = message;
        multicastReceiver.putAnnouncementMsgByIp(message.getGames(INDEX_ANNOUNCEMENT_MSG).getGameName(), ip, port, message);
    }
}