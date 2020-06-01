package ru.codebattle.client.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import static java.lang.Math.abs;
import static ru.codebattle.client.api.BoardElement.*;

public class GameBoard {

  public GameBoard(String boardString) {
    this.boardString = boardString.replace("\n", "");
  }

  @Getter
  private String boardString;

  public int size() {
    return (int) Math.sqrt(boardString.length());
  }

  public BoardPoint getMyHead() {
    return findFirstElement(
        HEAD_DEAD, HEAD_DOWN, HEAD_UP, HEAD_LEFT, HEAD_RIGHT, HEAD_EVIL, HEAD_FLY, HEAD_SLEEP);
  }

  @Getter
  public static BoardElement[] enemies = new BoardElement[] {ENEMY_HEAD_SLEEP, ENEMY_TAIL_INACTIVE,
      ENEMY_BODY_HORIZONTAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_DOWN,
      ENEMY_BODY_RIGHT_UP, ENEMY_BODY_VERTICAL, ENEMY_HEAD_EVIL, ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT,
      ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP, ENEMY_HEAD_FLY, ENEMY_HEAD_SLEEP, ENEMY_TAIL_END_DOWN,
      ENEMY_TAIL_END_LEFT, ENEMY_TAIL_END_UP, ENEMY_TAIL_END_RIGHT, ENEMY_TAIL_INACTIVE,
      ENEMY_BODY_HORIZONTAL, ENEMY_BODY_VERTICAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP,
      ENEMY_BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_UP };

  @Getter
  public static BoardElement[] bounties = new BoardElement[] {APPLE, GOLD, FURY_PILL, FLYING_PILL};

  @Getter
  public static BoardElement[] evilBounties =
      new BoardElement[] {
          APPLE, GOLD, FURY_PILL, FLYING_PILL, ENEMY_BODY_HORIZONTAL, ENEMY_BODY_LEFT_DOWN,
          ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_UP, ENEMY_BODY_VERTICAL,
          ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP, STONE
      };

  @Getter
  public static BoardElement[] barriers = new BoardElement[] {WALL, START_FLOOR, STONE};

  @Getter
  public static BoardElement[] evilBarriers = new BoardElement[] {WALL, START_FLOOR, ENEMY_HEAD_SLEEP};

  public List<BoardPoint> getWalls() {
    return findAllElements(WALL);
  }

  public List<BoardPoint> getStones() {
    return findAllElements(STONE);
  }

  public boolean isBarrierAt(BoardPoint point) {
    return getBarriers().contains(point);
  }

  public boolean isEvilBarrierAt(BoardPoint point) {
    return getBarriersEvil().contains(point);
  }

  public boolean isBountyAt(BoardPoint point) {
    return getBounty().contains(point);
  }

  public boolean isEvilBountyAt(BoardPoint point) {
    return getBountyEvil().contains(point);
  }

  public boolean isMyBody(BoardPoint point) {
    return getMyBody().contains(point);
  }

  public boolean isMyTail(BoardPoint point) {
    return getMyTail().contains(point);
  }

  public boolean isMe(BoardPoint point) {
    return isMyTail(point) || isMyBody(point) || (getMyHead().equals(point));
  }

  public int getMyLength() {
    return 2 + getMyBody().size();
  }

  public Direction getMyDirection() {
    BoardPoint head = getMyHead();
    BoardElement headElement = getElementAt(head);
    List myBody = getMyBody();
    Direction direction = Direction.STOP;
    switch (headElement) {
      case HEAD_DOWN:
        direction = Direction.DOWN;
        break;
      case HEAD_LEFT:
        direction = Direction.LEFT;
        break;
      case HEAD_UP:
        direction = Direction.UP;
        break;
      case HEAD_RIGHT:
        direction = Direction.RIGHT;
        break;
      case HEAD_EVIL:
      case HEAD_FLY:
        if (myBody.contains(head.shiftRight()) && !myBody.contains(head.shiftLeft()))
          direction = Direction.LEFT;
        if (myBody.contains(head.shiftLeft()) && !myBody.contains(head.shiftRight()))
          direction = Direction.RIGHT;
        if (myBody.contains(head.shiftBottom()) && !myBody.contains(head.shiftTop()))
          direction = Direction.UP;
        if (myBody.contains(head.shiftTop()) && !myBody.contains(head.shiftBottom()))
          direction = Direction.DOWN;
    }
    return direction;
  }

  public List<BoardPoint> getApples() {
    return findAllElements(APPLE);
  }

  public boolean amIEvil() {
    return findAllElements(HEAD_EVIL).contains(getMyHead());
  }

  public boolean amIFlying() {
    return findAllElements(HEAD_FLY).contains(getMyHead());
  }

  public List<BoardPoint> getFlyingPills() {
    return findAllElements(FLYING_PILL);
  }

  public List<BoardPoint> getFuryPills() {
    return findAllElements(FURY_PILL);
  }

  public List<BoardPoint> getGold() {
    return findAllElements(GOLD);
  }

  public List<BoardPoint> getStartPoints() {
    return findAllElements(START_FLOOR);
  }

  public List<BoardPoint> getBarriers() {
    return findAllElements(barriers);
  }

  public List<BoardPoint> getBarriersEvil() {
    return findAllElements(evilBarriers);
  }

  public List<BoardPoint> getBounty() {
    return findAllElements(bounties);
  }

  public List<BoardPoint> getBountyEvil() {
    return findAllElements(evilBounties);
  }

  public List<BoardPoint> getMyBody() {
    return findAllElements(BODY_HORIZONTAL, BODY_LEFT_DOWN, BODY_LEFT_UP, BODY_RIGHT_DOWN,
        BODY_RIGHT_UP, BODY_VERTICAL);
  }

  public List<BoardPoint> getMyTail() {
    return findAllElements(TAIL_END_DOWN, TAIL_END_LEFT, TAIL_END_RIGHT, TAIL_END_UP, TAIL_INACTIVE);
  }

  public List<BoardPoint> getEnemies() {
    return findAllElements(enemies);
  }

  public boolean hasElementAt(BoardPoint point, BoardElement element) {
    if (point.isOutOfBoard(size())) {
      return false;
    }

    return getElementAt(point) == element;
  }

  public BoardElement getElementAt(BoardPoint point) {
    return BoardElement.valueOf(boardString.charAt(getShiftByPoint(point)));
  }

  public void printBoard() {
    for (int i = 0; i < size(); i++) {
      System.out.println(boardString.substring(i * size(), size() * (i + 1)));
    }
  }

  public BoardPoint findElement(BoardElement elementType) {
    for (int i = 0; i < size() * size(); i++) {
      BoardPoint pt = getPointByShift(i);
      if (hasElementAt(pt, elementType)) {
        return pt;
      }
    }
    return null;
  }

  public BoardPoint findFirstElement(BoardElement... elementType) {
    for (int i = 0; i < size() * size(); i++) {
      BoardPoint pt = getPointByShift(i);

      for (BoardElement elemType : elementType) {
        if (hasElementAt(pt, elemType)) {
          return pt;
        }
      }
    }
    return null;
  }

  public List<BoardPoint> findAllElements(BoardElement... elementType) {
    List<BoardPoint> result = new ArrayList<>();

    for (int i = 0; i < size() * size(); i++) {
      BoardPoint pt = getPointByShift(i);

      for (BoardElement elemType : elementType) {
        if (hasElementAt(pt, elemType)) {
          result.add(pt);
        }
      }
    }

    return result;
  }

  public boolean hasElementAt(BoardPoint point, BoardElement... elements) {
    return Arrays.stream(elements).anyMatch(element -> hasElementAt(point, element));
  }

  private int getShiftByPoint(BoardPoint point) {
    return point.getY() * size() + point.getX();
  }

  private BoardPoint getPointByShift(int shift) {
    return new BoardPoint(shift % size(), shift / size());
  }

  public static int getDistance(BoardPoint p1, BoardPoint p2) {
    // метрика для поиска расстояния от головы змеи до элемента
    return abs(p1.getX() - p1.getX()) + abs(p1.getY() - p2.getY());
  }

  public BoardPoint findClosestElement(BoardElement elementType) {
    int distOld = size() * size();
    int distNew = distOld;
    BoardPoint resultPoint = null;
    BoardPoint head = getMyHead();
    for (int i = 0; i < size() * size(); i++) {
      BoardPoint pt = getPointByShift(i);
      if (hasElementAt(pt, elementType)) {
        // метрика для поиска расстояния от головы змеи до элемента
        distNew = getDistance(head, pt);
        if (distNew < distOld) {
          distOld = distNew;
          resultPoint = pt;
        }
      }
    }
    return resultPoint;
  }
}
