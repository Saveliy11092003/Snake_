package nsu.ccfit.ru.trushkov.game.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.ecxeption.*;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import static nsu.ccfit.ru.trushkov.context.ContextValue.*;
import static nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto.GameState.Snake.SnakeState.ALIVE;

@Getter
@Setter
@Slf4j
public class Snake implements Iterable<SnakesProto.GameState.Coord> {

    public static final int SNAKE_HEAD = 0;

    public static final int SNAKE_AFTER_HEAD = 1;

    public static final int MIN_SNAKE_ID = 2;

    public static final int ONE_SNAKE = 1;

    private static final int LEFT_MOVE = -1;

    private static final int RIGHT_MOVE = 1;

    private static final int UP_MOVE = -1;

    private static final int DOWN_MOVE = 1;

    @NotNull
    private SnakesProto.GameState.Coord head;

    @NotNull
    private SnakesProto.GameState.Coord tail;

    private List<SnakesProto.GameState.Coord> placement = new LinkedList<>();

    @NotNull
    private SnakesProto.Direction direction;

    @NotNull
    private SnakesProto.GameState.Snake.SnakeState state = ALIVE;

    private final Random random = new Random();

    private final int id;

    public Snake(@NotNull SnakesProto.GameState.Coord head,
                 @NotNull SnakesProto.GameState.Coord tail,
                 @NotNull Integer id) {
        log.info("init new snake");
        this.id = id;
        this.tail = tail;
        this.head = head;

        this.direction = this.chooseDirection();
        log.info("choose direction {}", this.direction);
        this.placement.add(tail);
        this.placement.add(head);
    }

    public Snake(@NotNull Integer id,
                 @NotNull List<SnakesProto.GameState.Coord> placement,
                 @NotNull SnakesProto.Direction direction) {
        this.id = id;
        this.placement.addAll(placement);
        this.direction = direction;
        this.head = this.placement.get(SNAKE_HEAD);
        this.tail =  this.placement.get(this.placement.size()-1);
    }

    public static SnakesProto.GameState.Snake createSnakeProto(Snake snake) {
        return SnakesProto.GameState.Snake.newBuilder().setHeadDirection(snake.getDirection())
                                                       .addAllPoints(snake.getPlacement())
                                                       .setPlayerId(snake.getId())
                                                       .setState(snake.getState())
                                                       .build();
    }

    public void changeState(SnakesProto.GameState.Snake.SnakeState state) {
        this.state = state;
    }

    public void addNewCoord(int index, SnakesProto.GameState.Coord coord) {
        if(index == SNAKE_HEAD) this.setHead(coord);
        this.placement.add(SNAKE_HEAD, coord);
        this.tail =  this.placement.get(this.placement.size()-1);
    }

    private SnakesProto.Direction chooseDirection() {
        log.info("head {} {} tail {} {} ", head.getX(), head.getY(), tail.getY (), tail.getX());
        if (head.getX() - tail.getX() == RIGHT_MOVE)
            return SnakesProto.Direction.RIGHT;
        else if (head.getX() - tail.getX() == LEFT_MOVE)
            return SnakesProto.Direction.LEFT;
        else if (head.getY() - tail.getY() == DOWN_MOVE)
            return SnakesProto.Direction.DOWN;
        else if(head.getY() - tail.getY() == UP_MOVE)
            return SnakesProto.Direction.UP;

        throw new ChooseDirectionException();
    }


    public static SnakesProto.GameState.Coord getNextCoord(int direction, SnakesProto.GameState.Coord center, Field field) {
        int y = center.getY();
        int x = center.getX();
        switch (direction) {
            case SnakesProto.Direction.UP_VALUE -> --y;
            case SnakesProto.Direction.DOWN_VALUE -> ++y;
            case SnakesProto.Direction.LEFT_VALUE -> --x;
            case SnakesProto.Direction.RIGHT_VALUE -> ++x;
            default -> throw new SwitchTailException();
        }
        return field.getCoord((x + field.getWidth()) % field.getWidth(),
                              (y + field.getHeight()) % field.getHeight());
    }

    public void move(SnakesProto.Direction direction, Field field, PlayerManager playerManager) {
        log.info("move snake");
        SnakesProto.GameState.Coord newHeadCoord = getNextCoord(direction.getNumber(), this.head, field);

        this.addNewCoord(SNAKE_HEAD, newHeadCoord);
        field.addCoordByID(newHeadCoord.getX(), newHeadCoord.getY(), this.id);

        List<Integer> headCell = field.getListValue(this.head);
        if(!headCell.contains(FOOD)) {
            field.getListValue(this.tail).remove(Integer.valueOf(this.id));
            placement.remove(this.tail);
        } else {
            playerManager.addPointByID(this.id);
            headCell.remove(Integer.valueOf(FOOD));
            field.getFoods().remove(this.head);
        }
    }

    @NotNull
    @Override
    public Iterator<SnakesProto.GameState.Coord> iterator() {
        return placement.iterator();
    }
}