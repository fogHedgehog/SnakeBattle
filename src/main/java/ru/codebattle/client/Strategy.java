package ru.codebattle.client;

import ru.codebattle.client.api.BoardElement;
import ru.codebattle.client.api.BoardPoint;
import ru.codebattle.client.api.Direction;
import ru.codebattle.client.api.GameBoard;

public class Strategy {

  static int evilTicks = 0;
  static Direction prevDirection = Direction.STOP;
  enum State {
    SLEEP, EVIL, USUAL, DEAD
  };
  static State prevState = State.SLEEP;

  final static int maxEvilTicks = 10;


  public static Direction getBestDirection(GameBoard gameBoard) {

    BoardPoint head = gameBoard.getMyHead();
    if (head == null) {
      return prevDirection;
    }

    BoardElement headElement = gameBoard.getElementAt(head);
    State curState = getMyState(headElement);

    if (curState == State.SLEEP) {
      prevState = State.SLEEP;
      prevDirection = Direction.STOP;
      return prevDirection;
    }
    if (curState == State.DEAD) {
      prevState = State.DEAD;
      prevDirection = Direction.STOP;
      return prevDirection;
    }

    if (prevState == State.SLEEP && curState == State.USUAL) {
      prevDirection = gameBoard.getMyDirection();
      prevState = State.USUAL;
    }
    if (prevState != State.EVIL && curState == State.EVIL) {
      evilTicks = maxEvilTicks;
      prevState = State.EVIL;
    } else if (evilTicks > 0) {
      evilTicks--;
      prevState = State.EVIL;
    } else {
      prevState = State.USUAL;
    }

    Direction bestDirection = PathFinder.Dijkstra(gameBoard, curState, evilTicks, prevDirection);

    prevDirection = bestDirection;

    return bestDirection;
  }

  public static boolean getBestAct() {
    return false;
  }

  public static State getMyState(BoardElement headElement) {
    State state = State.USUAL;
    if (headElement == BoardElement.HEAD_EVIL) {
      state = State.EVIL;
    } else if (headElement == BoardElement.HEAD_SLEEP) {
      state = State.SLEEP;
    } else if (headElement == BoardElement.HEAD_DEAD) {
      state = State.DEAD;
    }
    return state;
  }

  public static Direction getOppositeDirection(Direction direction) {
    Direction oppositeDirection = Direction.STOP;
    switch (direction) {
      case LEFT:
        oppositeDirection = Direction.RIGHT;
      break;
      case RIGHT:
        oppositeDirection = Direction.LEFT;
        break;
      case UP:
        oppositeDirection = Direction.DOWN;
        break;
      case DOWN:
        oppositeDirection = Direction.UP;
        break;
    }
    return oppositeDirection;
  }

}
