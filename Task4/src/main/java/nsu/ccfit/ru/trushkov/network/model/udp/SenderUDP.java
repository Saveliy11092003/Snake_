package nsu.ccfit.ru.trushkov.network.model.udp;

import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.network.model.message.Message;

import java.io.IOException;

import java.net.*;


@Slf4j
public class SenderUDP {

    private final DatagramSocket datagramSocket;

    public SenderUDP(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public void send(Message message) {
        try {
            datagramSocket.send(message.getPacket());
        } catch(IOException ex) {}
    }
}
