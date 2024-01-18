package nsu.ccfit.ru.trushkov.network.model.keynode;

import lombok.*;

import java.net.InetAddress;

@Data
@AllArgsConstructor
public class HostNetworkKey {
    private InetAddress ip;

    private int port;
}
