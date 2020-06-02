package ru.codebattle.client;

import java.io.IOException;
import java.net.URISyntaxException;

import ru.codebattle.client.api.*;

public class Main {

    private static final String SERVER_ADDRESS = "http://codebattle-pro-2020s1.westeurope.cloudapp.azure.com/codenjoy-contest/board/player/5e0atlzzxdpjg4nps3jy?code=6908131643316894529&gameName=snakebattle";
    public static void main(String[] args) throws URISyntaxException, IOException {
        SnakeBattleClient client = new SnakeBattleClient(SERVER_ADDRESS);
        client.run(
            gameBoard -> {
               // long startTime = System.currentTimeMillis();
                Direction direction = Strategy.getBestDirection(gameBoard);
                //long endTime = System.currentTimeMillis();
                //System.out.println("Total execution time: " + (endTime-startTime) + "ms");
                boolean act = Strategy.getBestAct();
                return new SnakeAction(act, direction);
            });

        System.in.read();

        client.initiateExit();
    }
}
