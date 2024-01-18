package nsu.ccfit.ru.trushkov.network;


import lombok.extern.slf4j.Slf4j;

import nsu.ccfit.ru.trushkov.ecxeption.ThreadInterException;
import nsu.ccfit.ru.trushkov.game.controller.GameController;
import nsu.ccfit.ru.trushkov.network.model.message.*;
import nsu.ccfit.ru.trushkov.network.model.thread.SenderScheduler;
import nsu.ccfit.ru.trushkov.network.model.udp.*;

import java.net.*;

@Slf4j
public class ServiceUDP {
    private static final int TIMEOUT_DELAY = 200;

    public static final int SEND_DELAY = 1;

    public static final int RECEIVE_DELAY = 1;

    private final DatagramSocket datagramSocket = new DatagramSocket();

    private final ReceiverUDP receiverUDP;

    private final SenderUDP senderUDP = new SenderUDP(datagramSocket);

    private final NetworkStorage networkStorage;

    public ServiceUDP(NetworkStorage networkStorage, GameController gameController) throws SocketException {
        receiverUDP = new ReceiverUDP(datagramSocket, gameController, networkStorage);
        this.networkStorage = networkStorage;
        datagramSocket.setSoTimeout(RECEIVE_DELAY);
    }

    public void startReceiver() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                long currentTime = System.currentTimeMillis();
                synchronized (datagramSocket) {
                    while (System.currentTimeMillis() - currentTime < TIMEOUT_DELAY / 10)
                        receiverUDP.receive();
                }
                try {
                    Thread.sleep(RECEIVE_DELAY);
                } catch (InterruptedException e) {
                    throw new ThreadInterException(e.getMessage());
                }
            }
        });
        thread.start();
    }

    public void startSender() {
        SenderScheduler senderScheduler = new SenderScheduler(networkStorage);
        senderScheduler.start();
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (datagramSocket) {
                    for(var message : networkStorage.getMessagesToSend()) {
                        senderUDP.send(message);
                        networkStorage.updateLastSendTime();
                        message.updateTimeSent();
                    }
                }
                try {
                    Thread.sleep(SEND_DELAY);
                } catch (InterruptedException e) {
                    throw new ThreadInterException(e.getMessage());
                }
            }
        });
        thread.start();
    }

    public void startCheckerMsgACK() {
        Runnable r = ()-> {
            while(!Thread.currentThread().isInterrupted()) {
                for(var sentMessage : networkStorage.getEntrySetSentMessage()) {
                     if(System.currentTimeMillis() - sentMessage.getValue().sentTime() > TIMEOUT_DELAY / 10) {
                         networkStorage.addMessageToSend(sentMessage.getValue().message());
                         networkStorage.removeSentMessage(sentMessage.getValue().message().getGameMessage().getMsgSeq());
                     }
                }
                try {
                    Thread.sleep(TIMEOUT_DELAY / 10);
                } catch (InterruptedException e) {
                    throw new ThreadInterException(e.getMessage());
                }
            }
        };
        new Thread(r).start();
    }
}

