package ru.codebattle.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.codebattle.client.Strategy.State;
import ru.codebattle.client.api.BoardElement;
import ru.codebattle.client.api.BoardPoint;
import ru.codebattle.client.api.Direction;
import ru.codebattle.client.api.GameBoard;

public class PathFinder {

  public static Direction BFS(GameBoard gameBoard, State state) {

    BoardPoint start = gameBoard.getMyHead();
    List<BoardPoint> frontier = new ArrayList<>();
    frontier.add(start);
    Map<BoardPoint, BoardPoint> cameFrom = new HashMap<>();
    cameFrom.put(start, null);
    BoardPoint goal = start;

    List<BoardPoint> bounties = gameBoard.getBounty();
    List<BoardPoint> evilBounties = gameBoard.getBountyEvil();
    evilBounties.addAll(gameBoard.getStones());

    List<BoardPoint> barriers = gameBoard.getBarriers();
    List<BoardPoint> evilBarriers = gameBoard.getBarriersEvil();

    List<BoardPoint> fullMe = gameBoard.getMyBody();
    fullMe.addAll(gameBoard.getMyTail());
    fullMe.add(gameBoard.getMyHead());

    barriers.addAll(fullMe);
    evilBarriers.addAll(fullMe);
    barriers.addAll(gameBoard.getEnemies());    // во врагов не врезаемся в обычном состоянии

    int size = gameBoard.size();
    int iterCount = 0;

    while (!frontier.isEmpty()) {
      iterCount++;
      BoardPoint current = frontier.remove(0);
      if (state == State.USUAL && bounties.contains(current)) {
        goal = current;
        break;
      }
      if (state == State.EVIL && evilBounties.contains(current)) {
        goal = current;
        break;
      }

      BoardPoint[] initNeighbors =
          new BoardPoint[] {
              current.shiftLeft(), current.shiftRight(), current.shiftTop(), current.shiftBottom()
          };
      List<BoardPoint> goodNeighbors = new ArrayList<>();

      for (BoardPoint neighbor : initNeighbors) {
        if (state == State.USUAL && !barriers.contains(neighbor) && !(neighbor.isOutOfBoard(size))) {
          goodNeighbors.add(neighbor);
        } else if (state == State.EVIL && !evilBarriers.contains(neighbor) && !(neighbor.isOutOfBoard(size))) {
          goodNeighbors.add(neighbor);
        }
      }

      for (BoardPoint next : goodNeighbors) {
        if (!cameFrom.containsKey(next)) {
          frontier.add(next);
          cameFrom.put(next, current);
        }
      }
    }
    // System.out.println("Число итераций поиска: " + iterCount);

    // ищем обратный путь
    BoardPoint current = goal;
    BoardPoint next = goal;
    while (current.notEquals(start)) {
      next = current;
      current = cameFrom.get(current);
    }

    Direction direction = getDirectionByShift(start, next);
    return direction;
  }


  public static List<BoardPoint> getNeighbors(GameBoard gameBoard, BoardPoint current, State curState) {
    BoardPoint left = current.shiftLeft();
    BoardPoint right = current.shiftRight();
    BoardPoint top = current.shiftTop();
    BoardPoint bottom = current.shiftBottom();

    BoardPoint[] initNeighbors = new BoardPoint[] {left, right, top, bottom};
    List<BoardPoint> goodNeighbors = new ArrayList<>();
    for (BoardPoint neighbor : initNeighbors) {
      if (curState == State.USUAL && !gameBoard.isBarrierAt(neighbor) && !gameBoard.isMe(neighbor)
          && !(gameBoard.getElementAt(neighbor) == BoardElement.ENEMY_TAIL_INACTIVE)
          && !(neighbor.isOutOfBoard(gameBoard.size()))) {
        goodNeighbors.add(neighbor);
      } else if (curState == State.EVIL && !gameBoard.isEvilBarrierAt(neighbor) && !gameBoard.isMe(neighbor)
          && !(neighbor.isOutOfBoard(gameBoard.size()))) {
        goodNeighbors.add(neighbor);
      }
    }

    return goodNeighbors;
  }


  public static Direction getDirectionByShift(BoardPoint center, BoardPoint shifted) {
    if (shifted.equals(center.shiftLeft())) {
      return Direction.LEFT;
    }
    if (shifted.equals(center.shiftRight())) {
      return Direction.RIGHT;
    }
    if (shifted.equals(center.shiftBottom())) {
      return Direction.DOWN;
    }
    if (shifted.equals(center.shiftTop())) {
      return Direction.UP;
    }
    return Direction.STOP;
  }

}
