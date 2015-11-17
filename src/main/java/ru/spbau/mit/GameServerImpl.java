package ru.spbau.mit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class GameServerImpl implements GameServer {
    private int lastId = 0;
    private Game plugin;
    private final Map<String, Connection> activeConnections = new HashMap<>();

    private String getSetterName(String setterName) {
        return "set" + setterName.toUpperCase().charAt(0) + setterName.substring(1);
    }

    private class GameServerRunnable implements Runnable {
        private final String playerId;
        private final Connection currentConnection;
        private final int timeout = 1000;

        public GameServerRunnable(String playerId, Connection currentConnection) {
            this.playerId = playerId;
            this.currentConnection = currentConnection;
        }

        @Override
        public void run() {
            plugin.onPlayerConnected(playerId);
            while (!currentConnection.isClosed()) {
                try {
                    synchronized (currentConnection) {
                        if (!currentConnection.isClosed()) {
                            String message = currentConnection.receive(timeout);
                            if (message != null) {
                                plugin.onPlayerSentMsg(playerId, message);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }

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
        if (!(plugin instanceof Game)) {
            throw new IllegalArgumentException("Wrong plugin " + gameClassName);
        }
    }

    @Override
    public void accept(final Connection connection) {
        synchronized (activeConnections) {
            activeConnections.put(String.valueOf(lastId), connection);
        }
        connection.send(String.valueOf(lastId));
        Thread connectionThread = new Thread(new GameServerRunnable(String.valueOf(lastId), connection));
        connectionThread.start();
        ++lastId;
    }

    @Override
    public void broadcast(String message) {
        synchronized (activeConnections) {
            for (Connection connection : activeConnections.values()) {
                connection.send(message);
            }
        }
    }

    @Override
    public void sendTo(String id, String message) {
        synchronized (activeConnections) {
            activeConnections.get(id).send(message);
        }
    }
}
