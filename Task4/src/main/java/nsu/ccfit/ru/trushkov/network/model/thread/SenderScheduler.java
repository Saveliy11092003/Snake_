package nsu.ccfit.ru.trushkov.network.model.thread;


import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.ecxeption.ThreadInterException;
import nsu.ccfit.ru.trushkov.network.model.message.*;

import static nsu.ccfit.ru.trushkov.network.ServiceUDP.SEND_DELAY;

@Slf4j
public class SenderScheduler extends Thread {
    private final NetworkStorage networkStorage;

    public SenderScheduler(NetworkStorage networkStorage) {
        this.networkStorage = networkStorage;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for(var message : networkStorage.getMessagesToSend()) {
                if(message.getTimeSent() == 0) continue;
                if(MessageType.isNeedConfirmation(message.getGameMessage().getTypeCase().getNumber()))
                    networkStorage.addSentMessage(message.getGameMessage().getMsgSeq(),
                                                  new NodeInfo(message.getHostNetworkKey(), message, System.currentTimeMillis()));
                networkStorage.removeMessageToSend(message);
            }
            try {
                Thread.sleep(SEND_DELAY);
            } catch (InterruptedException e) {
                throw new ThreadInterException (e.getMessage());
            }
        }
    }
}
