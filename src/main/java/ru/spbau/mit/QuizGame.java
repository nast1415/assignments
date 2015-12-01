package ru.spbau.mit;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class QuizGame implements Game {
    private GameServer server;

    private int delayUntilNextLetter = 0;
    private int maxLettersToOpen = 0;
    private int currentNumberOfLetters = 0;

    private List<String> questions = new ArrayList<>();
    private int currentQuestionIndex = -1;

    private List<String> answers = new ArrayList<>();

    private boolean isRunning = false;

    private Timer timer = new Timer();
    boolean isThisFirstRound = true;

    public void setDelayUntilNextLetter(Integer delayUntilNextLetter) {
        this.delayUntilNextLetter = delayUntilNextLetter;
    }

    public void setMaxLettersToOpen(Integer maxLettersToOpen) {
        this.maxLettersToOpen = maxLettersToOpen;
    }

    public void setDictionaryFilename(String dictionaryFilename) throws Exception {
        try {
            Scanner dictionary = new Scanner(new File(dictionaryFilename));
            while (dictionary.hasNextLine()) {
                String[] lines = dictionary.nextLine().split(";");
                questions.add(lines[0]);
                answers.add(lines[1]);
            }
            dictionary.close();
        } catch (FileNotFoundException e) {
            throw new Exception("Wrong dictionary filename. Dictionary file not found.", e);
        }
    }

    public void setNextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size();
    }

    public int getNumberOfLetters(int currentQuestionIndex) {
        return answers.get(currentQuestionIndex).length();
    }

    public QuizGame(GameServer server) {
        this.server = server;
    }

    @Override
    public void onPlayerConnected(String id) {

    }

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            if (currentNumberOfLetters == maxLettersToOpen) {
                server.broadcast("Nobody guessed, the word was " + answers.get(currentQuestionIndex));
                startRound();
            } else {
                currentNumberOfLetters++;
                server.broadcast("Current prefix is " + answers.get(currentQuestionIndex).substring(0, currentNumberOfLetters));
            }
        }
    }


    private synchronized void startRound() {
        setNextQuestion();
        server.broadcast("New round started: " + questions.get(currentQuestionIndex) + " (" + getNumberOfLetters(currentQuestionIndex) + " letters)");
        if (!isThisFirstRound) {
            timer.cancel();
        }

        isThisFirstRound = false;
        TimerTask task = new MyTimerTask();
        timer = new Timer();
        timer.schedule(task, delayUntilNextLetter, delayUntilNextLetter);
    }

    private synchronized void stopRound() {
        if (!isThisFirstRound) {
            timer.cancel();
        }
    }

    @Override
    public synchronized void onPlayerSentMsg(String id, String msg) {
        switch (msg) {
            case "!start":
                if (!isRunning) {
                    isRunning = true;
                    startRound();
                }
                break;

            case "!stop":
                if (isRunning) {
                    isRunning = false;
                    stopRound();
                    server.broadcast("Game has been stopped by " + id);
                }
                break;

            default:
                if (isRunning) {
                    if (msg.equals(answers.get(currentQuestionIndex))) {
                        server.broadcast("The winner is " + id);
                        stopRound();
                        startRound();
                    } else {
                        server.sendTo(id, "Wrong try");
                    }
                }

        }
    }
}
