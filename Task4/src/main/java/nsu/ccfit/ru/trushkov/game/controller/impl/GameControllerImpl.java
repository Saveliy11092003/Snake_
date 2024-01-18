package nsu.ccfit.ru.trushkov.game.controller.impl;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.ecxeption.*;
import nsu.ccfit.ru.trushkov.game.controller.*;

import nsu.ccfit.ru.trushkov.game.gui.GUIGameSpace;

import nsu.ccfit.ru.trushkov.game.model.*;
import nsu.ccfit.ru.trushkov.network.NetworkController;

import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;
import nsu.ccfit.ru.trushkov.network.model.gamemessage.GameMessage;
import nsu.ccfit.ru.trushkov.observer.*;
import nsu.ccfit.ru.trushkov.observer.context.*;

import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.net.*;
import java.util.Objects;

import static nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto.NodeRole.*;

@NoArgsConstructor
@Slf4j
public class GameControllerImpl extends Observable implements GameController {
    public static final int MASTER_PORT = 0;

    private static final int MAX_COUNT_SNAKE_ON_FIELD = 7;

    public static final String MASTER_IP = "localhost";

    public static final InetAddress inetAddressMASTER;

    private final ContextError contextError = new ContextError();

    static {
        try {
            inetAddressMASTER = InetAddress.getByName(MASTER_IP);
        } catch (UnknownHostException e) {
            throw new CastIpAddressException(MASTER_IP);
        }
    }

    private Game game;

    private PlayerManager playerManager;

    private GUIGameSpace guiGameSpace;

    private PlayerState playerState;

    private NetworkController networkController;

    private final ContextGame gameContext = new ContextGame();

    private SnakesProto.GameConfig gameConfig;

    @Override
    public void registrationGUIGameSpace(GUIGameSpace guiGameSpace){
        this.guiGameSpace = guiGameSpace;
    }

    public void registrationNetworkController(NetworkController networkController) {
        log.info("registration network controller");
        Objects.requireNonNull(networkController, "networkController cannot be null");
        this.networkController = networkController;
    }

    @Override
    public void subscriptionOnPlayerManager(ObserverNetwork observerNetwork) {
        this.playerManager.addObserverNetwork(observerNetwork);
    }

    @Override
    public void subscriptionOnMulticastService(ObserverGameState observerGameState) {
        this.networkController.subscriptionOnMulticastService(observerGameState);
    }

    public String getNameGame() {
        return this.playerState.nameGame();
    }

    @Override
    public void createConfigGame(String nameGame, String namePlayer, SnakesProto.GameConfig gameConfig){
        log.info("create game for user {}", nameGame);
        this.gameConfig = gameConfig;
        this.game = new Game(gameConfig);
        this.playerManager = new PlayerManager(nameGame, game);
        this.defaultLauncherForMaster();

        this.playerState = new PlayerState(playerManager.getCurrentPlayerID(), namePlayer, nameGame, MASTER);
        playerManager.createPlayer(inetAddressMASTER, MASTER_PORT, namePlayer, MASTER);
    }

    public void switchRoleToMaster(SnakesProto.GameState gameState) {
        this.networkController.closePlayerSchedulers();

        log.info("switchRoleToMaster");
        SnakesProto.GamePlayer gamePlayer = gameState.getPlayers().getPlayersList().stream()
                                    .filter(player -> player.getRole() == DEPUTY)
                                    .findFirst().orElseThrow(PlayerNotFoundException::new);

        this.playerState = new PlayerState(gamePlayer.getId(), playerState.playerName(), playerState.nameGame(), MASTER);
        this.game = new Game(gameConfig, gameState);
        this.playerManager = new PlayerManager(playerState.nameGame(), game, gameState.getPlayers());
        networkController.synchronizeMsgSeq();
        this.defaultLauncherForMaster();

        this.deletePlayer(inetAddressMASTER, MASTER_PORT);
        try {
            this.playerManager.updatePlayer(new HostNetworkKey(InetAddress.getByName(gamePlayer.getIpAddress()),
                                                                gamePlayer.getPort ()), MASTER);
        } catch(UnknownHostException ex) {
            log.warn("failed parse ip {}", ex.getMessage());
        }

        this.networkController.removeMaster();
    }

    private void defaultLauncherForMaster() {
        GameManager gameManager = new GameManager(game, playerManager);
        gameManager.addObserverState(this);
        this.playerManager.addObserverError(this);

        networkController.startMulticastSender(playerManager.getAnnouncementMsg());
        networkController.startSenderUDP();
        networkController.startMasterScheduler(gameConfig.getStateDelayMs());
        gameManager.run();
    }

    @Override
    public void initJoinGame(String playerName, String nameGame, SnakesProto.NodeRole role, SnakesProto.GameConfig gameConfig) {
        this.gameConfig = gameConfig;
        this.networkController.updateKeyMaster(networkController.getHostMasterByGame(nameGame), null);
        this.playerState = new PlayerState(null, playerName, nameGame, role);
        this.networkController.addRoleSelf(role);
        networkController.startPlayerSchedulers(gameConfig.getStateDelayMs());
    }
    @Override
    public void updatePlayer(HostNetworkKey hostNetworkKey, SnakesProto.NodeRole role) {
        this.playerManager.updatePlayer(hostNetworkKey, role);
    }

    @Override
    public void moveHandler(SnakesProto.Direction direction) {
        if (this.playerState.role() == MASTER)
            this.game.addMoveByKey(playerState.playerID(), direction);
        else
            networkController.addMessageToSend(this.playerState.nameGame(),
                                               GameMessage.createGameMessage(direction));
    }

    public void moveSnakeByHostKey(HostNetworkKey key, SnakesProto.Direction direction) {
        this.game.addMoveByKey(playerManager.getPlayerIDByHostNetwork(key), direction);
    }

    @Override
    public void sendMessageNetwork(String nameGame, SnakesProto.GameMessage gameMessage) {
        networkController.addMessageToSend(nameGame, gameMessage);
        networkController.startSenderUDP();
    }

    @Override
    public void joinToGame(HostNetworkKey hostNetworkKey, SnakesProto.GameMessage.JoinMsg message, SnakesProto.NodeRole role) {
        log.info("join to game ip {}, port {}", hostNetworkKey.getIp(), hostNetworkKey.getPort());
        if(game.getCountSnake() > MAX_COUNT_SNAKE_ON_FIELD) {
            contextError.update(hostNetworkKey, "the number of players has maxed out ");
            notifyObserversError(contextError);
            return;
        }

        this.playerManager.createPlayer(hostNetworkKey.getIp(), hostNetworkKey.getPort(),
                                        message.getPlayerName(), role);
    }

    @Override
    public void deletePlayer(InetAddress ip, int port){
        playerManager.deletePlayer(ip, port);
    }

    @Override
    public void updateState() {
        SnakesProto.GameMessage gameMessage = GameMessage.createGameMessage(game.getGameState(playerManager));

        if (this.playerState.role() == MASTER)
            for (var playerID : playerManager.getPlayersID().entrySet()) {
                if(playerID.getKey().getIp() != inetAddressMASTER)
                    networkController.addMessageToSend(playerID.getKey(), gameMessage);
            }

        this.updateStateGUI(gameMessage);
    }

    public void updateStateGUI(SnakesProto.GameMessage gameMessage) {
        gameContext.update(gameMessage);
        guiGameSpace.update(gameContext);
    }

    @Override
    public void updateError(ContextError context) {
        networkController.addMessageToSend(context.getHostNetworkKey(),
                                           GameMessage.createGameMessage(context.getMessage()));
    }

    @Override
    public void reportErrorGUI(String message) {
        guiGameSpace.printErrorMessage(message);
    }
}