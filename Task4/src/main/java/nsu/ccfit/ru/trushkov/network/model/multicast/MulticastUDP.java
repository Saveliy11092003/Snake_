package nsu.ccfit.ru.trushkov.network.model.multicast;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

@Slf4j
public abstract class MulticastUDP {

    public static final int TIMER_DELAY = 1000;

    public static final int MAX_SIZE_BUFFER  = 1024;

    protected final byte[] buffer = new byte[MAX_SIZE_BUFFER];

    protected final InetAddress ip;

    protected final int port;

    protected MulticastUDP(InetAddress ip, int port) {
        log.info ("init constructor by ip {} and port {}", ip, port);

        this.ip = ip;
        this.port = port;
    }
}
