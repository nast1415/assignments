package ru.spbau.mit;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class QuizGame implements Game {
    private GameServer server;
    Thread gameThread;

    private Integer delayUntilNextLetter = 0;
    private Integer maxLettersToOpen = 0;

    private List<String> questions = new ArrayList<>();
    private int currentQuestionIndex = -1;

    private List<String> answers = new ArrayList<>();

    private boolean isRunning = false;
    private boolean isStarted = false;
    private boolean isNewRoundStarted = false;

    public void setDelayUntilNextLetter(Integer delayUntilNextLetter) {
        this.delayUntilNextLetter = delayUntilNextLetter;
    }

    public void setMaxLettersToOpen(Integer maxLettersToOpen) {
        this.maxLettersToOpen = maxLettersToOpen;
    }

    public void setDictionaryFilename(String dictionaryFilename) {
        try {
            Scanner dictionary = new Scanner(new File(dictionaryFilename));
            while (dictionary.hasNextLine()) {
                String[] lines = dictionary.nextLine().split(";");
                questions.add(lines[0]);
                answers.add(lines[1]);
            }
            dictionary.close();
        } catch (FileNotFoundException e) {
            System.err.println("Wrong dictionary filename. Dictionary file not found.");
            System.exit(1);
        }
    }

    public void setNextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size();
    }

    public int getNumberOfLetters(int currentQuestionIndex) {
        return answers.get(currentQuestionIndex).length();
    }

    private class QuizGameRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (isRunning) {
                    setNextQuestion();
                    server.broadcast("New round started: " + questions.get(currentQuestionIndex) + " (" + getNumberOfLetters(currentQuestionIndex) + " letters)");
                    isNewRoundStarted = false;

                    for (int numberOfOpenedLetters = 0; numberOfOpenedLetters < maxLettersToOpen; numberOfOpenedLetters++) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
                        } catch (InterruptedException e) {
                            isNewRoundStarted = true;
                            break;
                        }
                        server.broadcast("Current prefix is " + answers.get(currentQuestionIndex).substring(0, numberOfOpenedLetters + 1));
                    }

                    if (isNewRoundStarted) {
                        continue;
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
                    } catch (InterruptedException e) {
                        isNewRoundStarted = true;
                        continue;
                    }

                    server.broadcast("Nobody guessed, the word was " + answers.get(currentQuestionIndex));
                }
            }
        }
    }

    public QuizGame(GameServer server) {
        this.server = server;
        gameThread = new Thread(new QuizGameRunnable());
    }

    @Override
    public void onPlayerConnected(String id) {

    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        synchronized (this) {
            switch (msg) {
                case "!start":
                    isRunning = true;
                    if (!isStarted) {
                        isStarted = true;
                        gameThread.start();
                    }
                    break;

                case "!stop":
                    isRunning = false;
                    gameThread.interrupt();
                    server.broadcast("Game has been stopped by " + id);
                    break;

                default:
                    if (msg.equals(answers.get(currentQuestionIndex))) {
                        server.broadcast("The winner is " + id);
                        gameThread.interrupt();
                    } else {
                        server.sendTo(id, "Wrong try");
                    }

            }
        }
    }
}
