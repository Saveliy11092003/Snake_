package nsu.ccfit.ru.trushkov.network.model.thread;

import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.network.model.gamemessage.GameMessage;
import nsu.ccfit.ru.trushkov.network.model.message.*;

@Slf4j
public class PingSender implements Runnable {
    private final NetworkStorage storage;

    private final int pingDelay;

    public PingSender(NetworkStorage storage, int delay) {
        this.storage= storage;
        this.pingDelay = delay / 10;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            if (System.currentTimeMillis() - storage.getLastSendTime() > this.pingDelay) {
                this.storage.addMessageToSend(new Message(storage.getMainRole().getKeyMaster(),
                                                          GameMessage.createGameMessage()));
                try {
                    Thread.sleep(pingDelay);
                } catch (InterruptedException e) {
                    log.warn("threwad ping was interrupted");
                    return;
                }
            }
        }
    }
}
