package nsu.ccfit.ru.trushkov.network.model.thread;

import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.game.controller.GameController;
import nsu.ccfit.ru.trushkov.network.model.message.NetworkStorage;

import static nsu.ccfit.ru.trushkov.context.ContextValue.DELAY;
import static nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto.NodeRole.DEPUTY;

@Slf4j
public class PlayerScheduler implements Runnable {

    private final GameController gameController;

    private final NetworkStorage storage;

    private final long delay;

    public PlayerScheduler(long delay, NetworkStorage storage, GameController gameController) {
        this.storage= storage;
        this.gameController = gameController;
        this.delay = delay;
    }

    private void changeMasterNode() {
        log.info("CHANGE MASTER NODE {}", storage.getMainRole().getRoleSelf());
        if (storage.getMainRole().getRoleSelf() == DEPUTY)
            gameController.switchRoleToMaster(storage.getCurrentStateGame());
        else
            storage.updateMainRole(this.storage.getMainRole().getKeyDeputy(), null);
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            if(!storage.isContainsMaster(delay))
                this.changeMasterNode();
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                log.warn("player thread was interrupted");
                return;
            }
        }
    }
}
