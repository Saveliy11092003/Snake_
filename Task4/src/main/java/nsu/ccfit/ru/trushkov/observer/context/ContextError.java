package nsu.ccfit.ru.trushkov.observer.context;

import lombok.Data;
import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;

@Data
public class ContextError implements Context {

    private String message;

    private HostNetworkKey hostNetworkKey;

    public void update(HostNetworkKey hostNetworkKey, String message) {
        this.hostNetworkKey = hostNetworkKey;
        this.message = message;
    }
}
