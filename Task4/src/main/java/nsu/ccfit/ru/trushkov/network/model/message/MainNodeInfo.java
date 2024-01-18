package nsu.ccfit.ru.trushkov.network.model.message;

import lombok.*;
import lombok.experimental.Accessors;
import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.util.Date;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class MainNodeInfo {

    private HostNetworkKey hostNetworkKey;

    private SnakesProto.GameMessage.AnnouncementMsg message;

    private Date date;
}
