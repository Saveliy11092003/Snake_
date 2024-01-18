package nsu.ccfit.ru.trushkov.network.model.message;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.ecxeption.TypeCaseException;
import nsu.ccfit.ru.trushkov.game.controller.GameController;
import nsu.ccfit.ru.trushkov.network.model.gamemessage.*;
import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.net.*;
import java.util.*;

@Slf4j
public final class MessageHandler implements Runnable {
    private final GameController gameController;

    private final HostNetworkKey hostNetworkKey;

    private final NetworkStorage storage;

    private final SnakesProto.GameMessage gameMessage;

    public MessageHandler(DatagramPacket packet, GameController gameController, NetworkStorage storage) {
        this.storage = storage;
        this.hostNetworkKey = new HostNetworkKey(packet.getAddress(), packet.getPort());
        try {
            this.gameMessage = SnakesProto.GameMessage.parseFrom(
                Arrays.copyOfRange(packet.getData(), 0, packet.getLength())
            );
            storage.updateLastSendTime();
            if(storage.isContainsPlayer(hostNetworkKey))
                this.storage.updaterDispatchTimePlayer(hostNetworkKey);

            this.sendNeedConfirmation(gameMessage.getTypeCase().getNumber());
        } catch (InvalidProtocolBufferException e) {
            throw new TypeCaseException(e);
        }
        this.gameController = gameController;
    }

    @Override
    public void run() {
        switch (gameMessage.getTypeCase()) {
            case PING ->  this.storage.updaterDispatchTimePlayer(hostNetworkKey);
            case STEER -> this.gameController.moveSnakeByHostKey(hostNetworkKey, gameMessage.getSteer().getDirection());
            case ACK -> this.storage.removeSentMessage(gameMessage.getMsgSeq());
            case STATE -> this.handlerState();
            case ERROR -> gameController.reportErrorGUI(gameMessage.getError().getErrorMessage());
            case ROLE_CHANGE -> this.handlerChangeRole();
            case DISCOVER -> handlerDiscover();
            case TYPE_NOT_SET -> log.info("message TYPE_NOT_SET");
            case JOIN -> this.handlerJoin();
            default -> throw new TypeCaseException();
        }
        this.storage.updaterDispatchTimePlayer(hostNetworkKey);
    }

    public void handlerDiscover() {
        this.storage.addMessageToSend(new Message(this.hostNetworkKey,
                                                  GameMessage.createGameMessage(this.storage.announcementMsgByNameGame(
                                                                                                    gameController.getNameGame()))));
    }

    public void sendNeedConfirmation(int typeCase) {
        if(MessageType.isNeedConfirmation(typeCase))
            storage.addMessageToSendFirst(new Message(hostNetworkKey,
                                          GameMessage.createGameMessage(gameMessage.getMsgSeq())));
    }

    private void handlerState() {
        if(gameMessage.getMsgSeq() < storage.getLastStateMsgNum()) return;

        Optional<SnakesProto.GamePlayer> deputyOptional = gameMessage.getState().getState()
                                                                 .getPlayers().getPlayersList()
                                                                 .stream().takeWhile(player -> player.getRole() != SnakesProto.NodeRole.DEPUTY)
                                                                 .findFirst();

        deputyOptional.ifPresent(deputy -> {
            try {
                this.storage.updateMainRole(this.hostNetworkKey,
                                            new HostNetworkKey(InetAddress.getByName(deputy.getIpAddress()),
                                                                                     deputy.getPort()));
            } catch (UnknownHostException e) {
                log.warn("failed to update main roles");
            }
        });
        this.storage.updateStateGame(gameMessage.getState().getState());
        this.storage.updateLastStateMsgNum(gameMessage.getMsgSeq());
        gameController.updateStateGUI(gameMessage);
    }


    private void handlerChangeRole() {
        if(this.gameMessage.getRoleChange().hasReceiverRole()) {
            log.info("handlerChangeRole {}", this.gameMessage.getRoleChange().getReceiverRole());
            this.storage.getMainRole().setRoleSelf(this.gameMessage.getRoleChange().getReceiverRole());
        }
    }

    private void handlerJoin() {
        SnakesProto.GameMessage.JoinMsg joinMsg = gameMessage.getJoin();

        SnakesProto.NodeRole rolePLayer = this.requestRole(joinMsg.getRequestedRole());
        this.gameController.joinToGame(hostNetworkKey, gameMessage.getJoin(), rolePLayer);
        this.storage.addNewUser(hostNetworkKey, new NodeRole(rolePLayer));
    }

    private SnakesProto.NodeRole requestRole(SnakesProto.NodeRole nodeRole) {
        if(nodeRole != SnakesProto.NodeRole.VIEWER && storage.getMainRole().getKeyDeputy() == null) {
            storage.updateMainRole(storage.getMainRole().getKeyMaster(), hostNetworkKey);

            this.storage.addMessageToSendFirst(new Message(hostNetworkKey,
                                               GameMessage.createGameMessage(ChangeMsg.create(SnakesProto.NodeRole.DEPUTY))));

            return SnakesProto.NodeRole.DEPUTY;
        }
        return nodeRole;
    }
}