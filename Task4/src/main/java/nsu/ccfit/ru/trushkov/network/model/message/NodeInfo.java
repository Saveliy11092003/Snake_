package nsu.ccfit.ru.trushkov.network.model.message;

import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;

public record NodeInfo(HostNetworkKey hostNetworkKey, Message message, long sentTime) {}
