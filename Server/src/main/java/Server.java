import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Server implements ConnectionListener {
    private static Logger serverLogger;

    private final ArrayList<Connection> connections = new ArrayList<>();
    private final ArrayList<Plugin> commands;

    private Server() {
        System.setProperty("log4j.configurationFile", "Server/src/main/resources/log4j.xml");
        serverLogger = LogManager.getLogger("Server.Server");

        serverLogger.info("New chat server is created");
        commands = PluginLoading.getPlugins();
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                try {
                    new Connection(serverSocket.accept(), this);
                } catch (IOException e) {
                    serverLogger.error("Error while creating connection: ", e);
                }
            }
        } catch (IOException e) {
            serverLogger.error(e);
            throw new RuntimeException(e);

        }
    }

    public static void main(String[] args) {
        new Server();
    }

    private String getCommandResult(String message) {
        for (Plugin command : commands) {
            if (command.isCommand(message))
                return command.getResult(message);
        }
        return "";
    }

    private boolean isPossibleCommand(String message) {
        int i = message.indexOf(":");
        String messageText = message.substring(i+2);
        return messageText.startsWith("/");
    }

    @Override
    public synchronized void onConnection(Connection connection) {
        connections.add(connection);
        notifyAll("Новый пользователь вступил в чат");
        serverLogger.info("Client connected: " + connection);
    }

    @Override
    public synchronized void onMessage(Connection connection, String message) {
        if (isPossibleCommand(message)) {
            String messageText = message.substring(message.indexOf(":")+2);
            String result = getCommandResult(messageText);
            if (!result.equals("")) {
                sendTo(connection, result);
                return;
            }
        }
        notifyAll(message);
    }

    @Override
    public synchronized void onDisconnect(Connection connection) {
        connections.remove(connection);
        notifyAll("Один из пользователей покинул чат");
        serverLogger.info("Client disconnected: " + connection);
    }

    @Override
    public synchronized void onException(Connection connection, Exception e) {
        serverLogger.error(e);
    }

    private void notifyAll(String msg) {
        for (Connection connection : connections) {
            connection.sendMessage(msg);
        }
        serverLogger.info("Message is sent to all connections: " + msg);
    }

    private void sendTo(Connection connection, String msg) {
        connection.sendMessage(msg);
    }
}
