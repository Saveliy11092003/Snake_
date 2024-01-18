package nsu.ccfit.ru.trushkov.game.model;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import nsu.ccfit.ru.trushkov.ecxeption.*;
import nsu.ccfit.ru.trushkov.protobuf.snakes.SnakesProto;

import java.util.*;

import static nsu.ccfit.ru.trushkov.context.ContextValue.*;
import static nsu.ccfit.ru.trushkov.game.model.Snake.SNAKE_HEAD;

@Slf4j
@Accessors(chain = true)
public class Field {

    private final List<LinkedList<Integer>> coords;

    private final Random random = new Random();

    @Getter
    private final List<SnakesProto.GameState.Coord> foods = new LinkedList<>();

    public static final int BEGIN_INDEX_COORD = 0;

    @Getter
    private final int width;

    @Getter
    private final int height;

    public Field(int width, int height) {
        this.coords = new ArrayList<>(width * height);
        for (int i = 0; i < width * height; ++i) {
            coords.add(new LinkedList<>());
        }
        log.info("size coords: {}", coords.size());
        this.width = width;
        this.height = height;
    }

    public Field(int width, int height, List<SnakesProto.GameState.Coord> foods,
                 List<SnakesProto.GameState.Snake> snakes) {
        this(width, height);

        foods.forEach(food -> {
            this.foods.add(food);
            this.addCoordByID(food.getX(), food.getY(), FOOD);
        });
        snakes.forEach(snake-> snake.getPointsList()
            .forEach(coord -> this.addCoordByID(coord.getX(), coord.getY(), snake.getPlayerId())));
    }

    public int getCountPlacementFood() {
        return foods.size();
    }

    public List<Integer> getListValue(int x, int y) {
        return this.coords.get(x + y * width);
    }

    public List<Integer> getListValue(SnakesProto.GameState.Coord coord) {
        return this.coords.get(coord.getX() + coord.getY() * width);
    }

    public void addCoordByID(int x, int y, int id) {
        this.coords.get(x + y * this.width).add(id);
    }

    public void setFoodByCoord(SnakesProto.GameState.Coord coord) {
        this.foods.add(coord);
    }

    public void foodPlacement(int count) {
        log.info("food placement by count {}", count);
        for(int i = 0, value; i < count; ++i) {
            while(!coords.get(value = this.random.nextInt(BEGIN_INDEX_COORD, height * width)).isEmpty());
            coords.get(value).add(FOOD);
            foods.add(this.getCoordByIndex(value));
        }
    }

    public void removeSnake(Snake snake) {
        log.info("remove snake by id " + snake.getId());
        List<SnakesProto.GameState.Coord> placementSnake = snake.getPlacement();
        for(var coord : placementSnake)
            this.getListValue(coord).remove(Integer.valueOf(snake.getId()));
    }

    private boolean isSuitableSquare(int beginX, int beginY) {
        List<Integer> listSnake = this.getListValue(beginX + OFFSET_CENTER, beginY + OFFSET_CENTER);
        if(!listSnake.isEmpty() && listSnake.get(SNAKE_HEAD) > FOOD)
            return false;

        for(int y = 0, emptyRows = 0; y < SIZE_SQUARE; ++y) {
            int emptyLine = 0;
            for(int x = 0; x < SIZE_SQUARE && emptyLine < SNAKE_PIT; ++x) {
                emptyLine = (this.getListValue(x + beginX, y + beginY).isEmpty()) ? ++emptyLine : EMPTY_SQUARE_SIZE;
                if(emptyLine == SNAKE_PIT) ++emptyRows;
            }
            if(emptyRows == SNAKE_PIT) return true;
        }
        return false;
    }

    public SnakesProto.GameState.Coord findPlaceHeadSnake() throws FindSuitableSquareException {
        for(int y = 0; y < this.height - BOUNDARY_Y; ++y) {
            for(int x = 0; x < this.width - BOUNDARY_X; ++x) {
                if(isSuitableSquare(x, y))
                    return SnakesProto.GameState.Coord.newBuilder()
                                                        .setX(x + OFFSET_CENTER)
                                                        .setY(y + OFFSET_CENTER)
                                                        .build();
            }
        }
        throw new FindSuitableSquareException();
    }

    public SnakesProto.GameState.Coord getCoord(int x, int y) {
        return SnakesProto.GameState.Coord.newBuilder()
            .setX(x).setY(y).build();
    }

    public boolean containsFood(SnakesProto.GameState.Coord coord) {
        return foods.contains(coord);
    }

    public SnakesProto.GameState.Coord getCoordByIndex(int index) {
        int y = index / width;
        return this.getCoord(index - y * width, y);
    }

    public int switchCoordsDirection(int direction, int center) {
        log.info("coord in method switchCoordsDirection {}", direction);
        return switch (direction) {
            case SnakesProto.Direction.UP_VALUE -> center - this.width;
            case SnakesProto.Direction.DOWN_VALUE -> center + this.width;
            case SnakesProto.Direction.LEFT_VALUE -> center - LEFT_VALUE;
            case SnakesProto.Direction.RIGHT_VALUE -> center + RIGHT_VALUE;
            default -> throw new SwitchTailException();
        };
    }

    private boolean isCorrectIndexCoords(int index) {
        return index >= 0 && index <= this.width * this.height;
    }

    public SnakesProto.GameState.Coord findPlaceTailSnake(int center) throws FindSuitableSquareException {
        log.info("find place tail snake");
        int coord = this.random.nextInt(NUMBER_OPTION_TAIL);
        log.info("random coord = {}", coord);
        int position;
        int selectOptions  = 0;
        do {
             position = switchCoordsDirection(++coord % NUMBER_OPTION_TAIL + 1, center);
             if(++selectOptions > NUMBER_OPTION_TAIL) throw new FindSuitableSquareException();
        } while(this.isCorrectIndexCoords(position) && !coords.get(position).isEmpty());

        int y = position / width;
        log.info("find coord: {}", position);
        return SnakesProto.GameState.Coord.newBuilder()
                                          .setX(position - width * y).setY(y).build();
    }
}