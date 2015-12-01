package ru.spbau.mit;

import java.util.*;


public class SumTwoNumbersGame implements Game {
    private int firstGuessNumber, secondGuessNumber;
    private final GameServer server;
    private final Random random = new Random();

    public SumTwoNumbersGame(GameServer server) {
        setNumbers();
        this.server = server;
    }

    private void setNumbers() {
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
    public synchronized void onPlayerSentMsg(String id, String msg) throws Exception {
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
            throw new Exception("Your answer is not a number.", e);
        }
    }
}
