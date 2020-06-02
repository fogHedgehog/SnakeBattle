package ru.codebattle.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.codebattle.client.Strategy.State;
import ru.codebattle.client.api.BoardElement;
import ru.codebattle.client.api.BoardPoint;
import ru.codebattle.client.api.Direction;
import ru.codebattle.client.api.GameBoard;

public class PathFinder {

  private static int bountyChoseCount = 3;    // max число bounty, которые будем рассматривать
  private static int evilTicks = 0;
  private static int myLength = 0;    // моя текущая длина

  private static Comparator<BoardElement> comparatorBounties = (el1, el2) -> {   // APPLE, GOLD, FURY_PILL, FLYING_PILL
    if (el1.equals(el2)) {return 0; }
    if (el1 == BoardElement.FURY_PILL)  { return -1; }
    if (el2 == BoardElement.FURY_PILL)  { return 1; }
    if (el1 == BoardElement.APPLE && myLength < 3) {return -1; }
    if (el2 == BoardElement.APPLE && myLength < 3) {return 1; }
    if (el1 == BoardElement.GOLD) {return -1; }
    if (el2 == BoardElement.GOLD) {return 1; }
    if (el1 == BoardElement.APPLE) {return 1;}
    if (el2 == BoardElement.APPLE) {return -1;}
    return 0;
  };

  private static Comparator<BoardElement> comparatorEvilBounties = (el1, el2) -> {
    if (el1.equals(el2)) {return 0; }
    if (el1 == BoardElement.FURY_PILL && evilTicks < 5)  { return -1; }
    if (el2 == BoardElement.FURY_PILL && evilTicks < 5)  { return 1; }
    if (el1 == BoardElement.FURY_PILL && evilTicks >= 5)  { return 1; }
    if (el2 == BoardElement.FURY_PILL && evilTicks >= 5)  { return -1; }
    if (el1 == BoardElement.APPLE && myLength < 3) {return -1; }
    if (el2 == BoardElement.APPLE && myLength < 3) {return 1; }
    if (el1 == BoardElement.GOLD) {return -1; }
    if (el2 == BoardElement.GOLD) {return 1; }
    if (el1 == BoardElement.STONE && evilTicks < 1) {return 1;}
    if (el2 == BoardElement.STONE && evilTicks < 1) {return -1;}
    if (el1 == BoardElement.APPLE) {return 1;}
    if (el2 == BoardElement.APPLE) {return -1;}
    return 0;
  };

  public static Direction Dijkstra(GameBoard gameBoard, State state, int evilCount, Direction prevDirection) {

    evilTicks = evilCount;
    // System.out.println("evil Ticks: " + evilTicks);
    BoardPoint start = gameBoard.getMyHead();
    Map<BoardPoint, Integer> frontier = new HashMap<>();  // очередь с приоритетом
    frontier.put(start, 0);
    Map<BoardPoint, BoardPoint> cameFrom = new HashMap<>();
    cameFrom.put(start, null);
    Map<BoardPoint, Integer> costNow = new HashMap();
    costNow.put(start, 0);

    BoardPoint goal = start;

    BoardPoint back = null;
    if (prevDirection != Direction.STOP) {
      back = getShiftedPointByOppositeDirection(start, prevDirection);
    }

    List<BoardPoint> bounties = gameBoard.getBounty();
    List<BoardPoint> evilBounties = gameBoard.getBountyEvil();

    List<BoardPoint> barriers = gameBoard.getBarriers();
    List<BoardPoint> evilBarriers = gameBoard.getBarriersEvil();
    List<BoardPoint> stones = gameBoard.getStones();

    List<BoardPoint> myBody = gameBoard.getMyBody();
    List<BoardPoint> myTail = gameBoard.getMyTail();
    List<BoardPoint> fullMe = new ArrayList<>();
    fullMe.addAll(myBody);
    fullMe.addAll(myTail);
    fullMe.add(gameBoard.getMyHead());

    myLength = fullMe.size();

    List<BoardPoint> enemies = gameBoard.getEnemies();

    List<BoardPoint> consideredBounties = new ArrayList<>();

    int size = gameBoard.size();
    int iterCount = 0;
    int foundBountyCount = 0;
    int afterFirstFoundBountyCount = 0;
    boolean foundFirstBounty = false;

    while (!frontier.isEmpty() || afterFirstFoundBountyCount < 11) {
      iterCount++;
      if (foundFirstBounty) {afterFirstFoundBountyCount++;}
      BoardPoint current = getFromPriorityQueue(frontier);
      frontier.remove(current);

      if ((state == State.USUAL && bounties.contains(current)) ||
          (state == State.EVIL && evilBounties.contains(current))) {
        foundBountyCount++;
        consideredBounties.add(current);
        if (foundBountyCount == 1)
          {foundFirstBounty = true;}

        if (foundBountyCount == bountyChoseCount) {
          break;
        }
      }

      BoardPoint[] initNeighbors =
          new BoardPoint[] {
              current.shiftLeft(), current.shiftRight(), current.shiftTop(), current.shiftBottom()
          };
      List<BoardPoint> goodNeighbors = new ArrayList<>();

      for (BoardPoint neighbor : initNeighbors) {
        if (!neighbor.equals(back)) {
          if (state == State.USUAL && !barriers.contains(neighbor) && !(neighbor.isOutOfBoard(size))) {
            goodNeighbors.add(neighbor);
          } else if (state == State.EVIL && !evilBarriers.contains(neighbor) && !(neighbor.isOutOfBoard(size))) {
            goodNeighbors.add(neighbor);
          }
        }
      }

      for (BoardPoint next : goodNeighbors) {
        BoardElement elem = gameBoard.getElementAt(next);
        int newCost = costNow.get(current) + cost(next, elem, state, myBody, myTail, enemies, stones);
        if (!costNow.containsKey(next) || (newCost < costNow.get(next))) {
          costNow.put(next, newCost);
          frontier.put(next, newCost);
          cameFrom.put(next, current);
        }
      }
    }
   // System.out.println("Число итераций поиска: " + iterCount);


    List<BoardElement> initBountyElements = consideredBounties.stream().map(x -> gameBoard.getElementAt(x))
        .collect(Collectors.toList());
   // System.out.println("found Bounties" + initBountyElements.toString());

    // ищем обратный путь
    if (state == State.USUAL) {
      goal = getBestGoal(consideredBounties, initBountyElements, comparatorBounties);
    } else if (state == State.EVIL) {
      goal = getBestGoal(consideredBounties, initBountyElements, comparatorEvilBounties);
    }
    BoardPoint current = goal;
    BoardPoint next = goal;
    while (current.notEquals(start)) {
      next = current;
      current = cameFrom.get(current);
    }

    Direction direction = getDirectionByShift(start, next);
    return direction;
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

  public static int cost(BoardPoint point, BoardElement elem, State state, List<BoardPoint> body, List<BoardPoint> tail,
      List<BoardPoint> enemies, List<BoardPoint> stones) {    // веса для небарьеров и ненаград

    if (tail.contains(point)) { return 6; }
    if (body.contains(point)) { return 9; }
    if (state == State.USUAL) {
      if (stones.contains(point) && myLength > 5 ) { return 7; }
      if (stones.contains(point) && myLength <= 5 ) { return 15; }
      if (enemies.contains(point) && myLength > 5) { return 10; }
      if (enemies.contains(point) && myLength <= 5) { return 15; }
    } else if (state == State.EVIL) {
      if (elem == BoardElement.ENEMY_HEAD_EVIL) { return 20; }
    }

    return 1;
  }

  public static BoardPoint getBestGoal(List<BoardPoint> bounties, List<BoardElement> initBountyElements,
      Comparator<BoardElement> comparator) {    // выбор лучшей bounty из предложенных по comparator
    List<BoardElement> bountyElements = new ArrayList<>(initBountyElements);
    bountyElements.sort(comparator);
   // System.out.println("sorted Bounties" + bountyElements.toString());
    BoardElement bestBounty = bountyElements.get(0);
    int bestGoalIndex = initBountyElements.indexOf(bestBounty);
    return bounties.get(bestGoalIndex);
  }

  public static BoardPoint getFromPriorityQueue(Map<BoardPoint, Integer> priorityQueue) {
    Collection<Integer> priorities = priorityQueue.values();
    Integer maxPriority = Collections.min(priorities);
    for (BoardPoint elem : priorityQueue.keySet()) {
      if (priorityQueue.get(elem) == maxPriority) {
        return elem;
      }
    }
    return null;
  }

  public static BoardPoint getShiftedPointByOppositeDirection(BoardPoint center, Direction direction) {
    BoardPoint shiftedPoint = null;
    switch (direction) {
      case LEFT:
        shiftedPoint = center.shiftRight();
        break;
      case RIGHT:
        shiftedPoint = center.shiftLeft();
        break;
      case UP:
        shiftedPoint = center.shiftBottom();
        break;
      case DOWN:
        shiftedPoint = center.shiftTop();
        break;
    }
    return shiftedPoint;
  }

}
