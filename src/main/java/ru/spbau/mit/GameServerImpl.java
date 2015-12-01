package ru.spbau.mit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


public class GameServerImpl implements GameServer {
    private int lastId = 0;
    private final Game plugin;
    private final Map<String, GameServerRunnable> activeConnections = new HashMap<>();

    private String getSetterName(String setterName) {
        return "set" + setterName.toUpperCase().charAt(0) + setterName.substring(1);
    }

    private class GameServerRunnable implements Runnable {
        private final String playerId;
        private final Connection currentConnection;
        private final int timeout = 1000;
        public final Queue<String> messages = new ConcurrentLinkedDeque<>();

        public GameServerRunnable(String playerId, Connection currentConnection) {
            this.playerId = playerId;
            this.currentConnection = currentConnection;
        }

        @Override
        public void run() {
            plugin.onPlayerConnected(playerId);
            while (!currentConnection.isClosed()) {
                try {
                    synchronized (activeConnections) {
                        if (!currentConnection.isClosed()) {
                            String message = currentConnection.receive(timeout);
                            if (message != null) {
                                plugin.onPlayerSentMsg(playerId, message);
                            }

                            if (!messages.isEmpty()) {
                                String currentMessage = messages.poll();
                                currentConnection.send(currentMessage);
                            }
                        }

                    }
                } catch (Exception e) {
                    break;
                }
            }
            activeConnections.remove(playerId);

        }
    }

    public GameServerImpl(String gameClassName, Properties properties) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.plugin = (Game) Class.forName(gameClassName).getConstructor(GameServer.class).newInstance(this);

        for (String connectionKey : properties.stringPropertyNames()) {
            String connectionValue = properties.getProperty(connectionKey);
            String setterName = getSetterName(connectionKey);
            try {
                int intConnectionValue = Integer.parseInt(connectionValue);
                Method setter = Class.forName(gameClassName).getMethod(setterName, Integer.class);
                setter.invoke(plugin, intConnectionValue);
            } catch (NumberFormatException e) {
                Method setter = Class.forName(gameClassName).getMethod(setterName, String.class);
                setter.invoke(plugin, connectionValue);
            }
        }
    }

    @Override
    public void accept(final Connection connection) {
        GameServerRunnable runnable = new GameServerRunnable(String.valueOf(lastId), connection);
        synchronized (activeConnections) {
            activeConnections.put(String.valueOf(lastId), runnable);
        }
        connection.send(String.valueOf(lastId));
        Thread connectionThread = new Thread(runnable);
        connectionThread.start();
        ++lastId;
    }

    @Override
    public void broadcast(String message) {
        synchronized (activeConnections) {
            for (GameServerRunnable el : activeConnections.values()) {
                synchronized (el.messages) {
                    el.messages.add(message);
                }
            }
        }
    }

    @Override
    public void sendTo(String id, String message) {
        synchronized (activeConnections) {
            GameServerRunnable task = activeConnections.get(id);
            synchronized (task.messages) {
                task.messages.add(message);
            }
        }
    }
}
