package ru.spbau.mit;

import java.util.*;


public class SumTwoNumbersGame implements Game {
    private int firstGuessNumber, secondGuessNumber;
    private final GameServer server;

    public SumTwoNumbersGame(GameServer server) {
        setNumbers();
        this.server = server;
    }

    private void setNumbers() {
        Random random = new Random();
        firstGuessNumber = random.nextInt(100);
        secondGuessNumber = random.nextInt(100);
    }

    private String getMessage() {
        return String.valueOf(firstGuessNumber) + " " + String.valueOf(secondGuessNumber);
    }

    @Override
    public synchronized void onPlayerConnected(String id) {
        server.sendTo(id, getMessage());
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        try {
            int sum = Integer.parseInt(msg);
            if (sum == firstGuessNumber + secondGuessNumber) {
                server.sendTo(id, "Right");
                server.broadcast(id + "won");
                setNumbers();
                server.broadcast(getMessage());
            } else {
                server.sendTo(id, "Wrong");
            }
        } catch (NumberFormatException e) {
            System.err.println("Your answer is not a number.");
            System.exit(1);
        }
    }
}
