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
        serverLogger.info("Plugins are loaded");
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                try {
                    new Connection(serverSocket.accept(), this);
                } catch (IOException e) {
                    serverLogger.error("Error while creating connection");
                    serverLogger.trace(e);
                }
            }
        } catch (IOException e) {
            serverLogger.trace(e);
            throw new RuntimeException(e);

        }
    }

    public static void main(String[] args) {
        new Server();
    }

    synchronized private void computeCommand(Connection connection, String command) {
        CommandThread commandThread = new CommandThread(commands, command, this, connection);
        commandThread.start();
        serverLogger.debug("Created new commandThread " + commandThread);
    }

    synchronized void onCommandFinished(CommandThread commandThread, Connection connection) {
        serverLogger.debug("Command calculation finished with result " + commandThread.getResult());
        commandThread.interrupt();
        sendTo(connection, commandThread.getResult());
    }

    private boolean isCommand(String message) {
        int i = message.indexOf(":");
        String messageText = message.substring(i + 2);
        for (Plugin command : commands)
            if (command.isCommand(messageText))
                return true;
        return false;
    }

    @Override
    public synchronized void onConnection(Connection connection) {
        connections.add(connection);
        notifyAll("Новый пользователь вступил в чат");
        serverLogger.info("Client connected: " + connection);
    }

    @Override
    public synchronized void onMessage(Connection connection, String message) {
        if (isCommand(message)) {
            String messageText = message.substring(message.indexOf(":") + 2);
            computeCommand(connection, messageText);
            serverLogger.debug("Started to compute command from " + connection.toString() + ": " + messageText);
        } else notifyAll(message);
    }

    @Override
    public synchronized void onDisconnect(Connection connection) {
        connections.remove(connection);
        notifyAll("Один из пользователей покинул чат");
        serverLogger.debug("Client disconnected: " + connection);
        serverLogger.info("One client disconnected");
    }

    @Override
    public synchronized void onException(Connection connection, Exception e) {
        serverLogger.error(e);
    }

    private void notifyAll(String msg) {
        for (Connection connection : connections) {
            connection.sendMessage(msg);
        }
        serverLogger.debug("Message is sent to all connections: " + msg);
        serverLogger.info("Message is sent");
    }

    private void sendTo(Connection connection, String msg) {
        connection.sendMessage(msg);
        serverLogger.debug("Send message(" + msg + ") to " + connection.toString());
        serverLogger.info("Message is sent");
    }
}
