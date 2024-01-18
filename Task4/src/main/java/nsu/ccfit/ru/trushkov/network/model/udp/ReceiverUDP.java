package nsu.ccfit.ru.trushkov.network.model.udp;

import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.game.controller.GameController;
import nsu.ccfit.ru.trushkov.network.model.message.*;

import java.io.IOException;
import java.net.*;

import java.util.concurrent.*;

import static nsu.ccfit.ru.trushkov.context.ContextValue.SIZE_BUFFER;

@Slf4j
public class ReceiverUDP {

    private final DatagramSocket datagramSocket;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final byte[] buffer = new byte[SIZE_BUFFER];
    private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    private final GameController gameController;

    private final NetworkStorage storage;

    public ReceiverUDP(DatagramSocket datagramSocket, GameController gameController, NetworkStorage storage) {
        this.datagramSocket = datagramSocket;
        this.gameController = gameController;
        this.storage = storage;
    }

    public void receive() {
        try {
            datagramSocket.receive(packet);
            executorService.submit(new MessageHandler(packet, gameController, storage));
        } catch (IOException ignored) {}
    }
}