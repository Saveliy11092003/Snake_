package nsu.ccfit.ru.trushkov.game.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.ecxeption.FindSuitableSquareException;
import nsu.ccfit.ru.trushkov.network.model.keynode.HostNetworkKey;
import nsu.ccfit.ru.trushkov.observer.Observable;
import nsu.ccfit.ru.trushkov.observer.context.*;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.net.*;
import java.util.*;

import static nsu.ccfit.ru.trushkov.game.model.Snake.MIN_SNAKE_ID;

@Slf4j
public class PlayerManager extends Observable {
    @Getter
    private final Map<HostNetworkKey, Integer> playersID = new HashMap<>();

    private final Map<Integer, SnakesProto.GamePlayer> players = new HashMap<>();

    private final ContextMainNodeInfo contextMainNodeInfo = new ContextMainNodeInfo();

    private final ContextError contextError = new ContextError();

    private static final int BEGIN_POINT = 0;

    private static final int FOOD_POINT = 1;

    @Getter
    private int currentPlayerID = MIN_SNAKE_ID;

    private final Game game;

    @Getter
    private final String nameGame;

    public PlayerManager(String nameGame, Game game) {
        this.game = game;
        this.nameGame = nameGame;
    }

    public PlayerManager(String nameGame, Game game, SnakesProto.GamePlayers gamePlayers) {
        this(nameGame, game);
        gamePlayers.getPlayersList().forEach(player -> parseInetAddr(player.getIpAddress()).ifPresent(resolvedIPAddress -> {
                                             playersID.put(new HostNetworkKey(resolvedIPAddress, player.getPort()), player.getId());
                                             players.put(player.getId(), player);
        }));
    }

    public Integer getPlayerIDByHostNetwork(HostNetworkKey key) {
        return playersID.get(key);
    }

    public List<SnakesProto.GamePlayer> listPlayers() {
        return this.players.values().stream().toList();
    }

    private void addNewUserByIP(HostNetworkKey key, SnakesProto.GamePlayer player) {
        playersID.put(key, player.getId());
        players.put(player.getId(), player);
    }

    private SnakesProto.GamePlayer buildPlayer(Integer id, String nameUser, String ip, int port, SnakesProto.NodeRole role, int point) {
        return SnakesProto.GamePlayer.newBuilder().setName(nameUser).setId(id).setPort(port)
                                                  .setRole(role).setIpAddress(ip).setScore(point)
                                                  .build();
    }

    private Optional<InetAddress> parseInetAddr(String ip) {
        try {
            InetAddress resolvedAddress = InetAddress.getByName(ip);
            return Optional.of(resolvedAddress);
        } catch (UnknownHostException e) {
            log.warn("Failed to parse IP address: " + ip, e);
            return Optional.empty();
        }
    }

    public void createPlayer(InetAddress ip, int port, String nameUser, SnakesProto.NodeRole role) {
        SnakesProto.GamePlayer player;

        player = buildPlayer(this.currentPlayerID, nameUser, ip.getHostAddress(), port, role, BEGIN_POINT);

        HostNetworkKey hostNetworkKey = new HostNetworkKey(ip, port);
        try {
            if (role != SnakesProto.NodeRole.VIEWER)
                this.game.createSnake(this.currentPlayerID);
        } catch(FindSuitableSquareException ex) {
            contextError.update(hostNetworkKey, "refusal to join the game because there is no room on the pitch");
            super.notifyObserversError(contextError);
        }

        this.addNewUserByIP(hostNetworkKey, player);
        this.contextMainNodeInfo.update(ip, port, this.getAnnouncementMsg());

        this.playersID.put(hostNetworkKey, this.currentPlayerID++);
        this.notifyObserversNetwork(contextMainNodeInfo);
    }

    public void updatePlayer(HostNetworkKey hostNetworkKey, SnakesProto.NodeRole role) {
        SnakesProto.GamePlayer player = players.get(playersID.get(hostNetworkKey));
        this.players.put(playersID.get(hostNetworkKey), this.buildPlayer(player.getId(), player.getName(),
                         player.getIpAddress(), player.getPort(), role, player.getScore()));

        this.updateContext(player.getIpAddress(), player.getPort());
    }

    public SnakesProto.GameAnnouncement createGameAnnouncement() {
        return SnakesProto.GameAnnouncement.newBuilder()
                                    .setGameName(this.nameGame)
                                    .setConfig(this.game.getGameConfig())
                                    .setCanJoin(true)
                                    .setPlayers(SnakesProto.GamePlayers.newBuilder()
                                                .addAllPlayers(this.listPlayers()))
                                    .build();
    }

    public SnakesProto.GameMessage.AnnouncementMsg getAnnouncementMsg() {
        return SnakesProto.GameMessage.AnnouncementMsg
                                      .newBuilder()
                                      .addGames(this.createGameAnnouncement())
                                      .build();
    }

    private void updateContext(String hostIP, int hostPort) {
        parseInetAddr(hostIP).ifPresent(resolvedIPAddress -> {
            this.contextMainNodeInfo.update(resolvedIPAddress, hostPort, this.getAnnouncementMsg());
            this.notifyObserversNetwork(contextMainNodeInfo);
        });
    }

    public void deletePlayer(InetAddress ip, int port) {
        log.info("DELETE PLAYER {} {}", ip , port);
        Integer id = playersID.get(new HostNetworkKey(ip, port));
        this.players.remove(id);
        game.changeStatusPlayerSnake(id, SnakesProto.GameState.Snake.SnakeState.ZOMBIE);
        this.updateContext(ip.getHostAddress(), port);
    }

    public void addPointByID(Integer id) {
        SnakesProto.GamePlayer player = players.get(id);
        if(player == null) return;

        players.put(id, this.buildPlayer(id, player.getName(), player.getIpAddress(),
                                         player.getPort(), player.getRole(), player.getScore() + FOOD_POINT));
        this.updateContext(player.getIpAddress(), player.getPort());
    }
}