import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Server implements ConnectionListener {
    private static Logger serverLogger;

    private final ArrayList<Connection> connections = new ArrayList<>();
    private final ArrayList<Plugin> commands;
    private final CommandScheduler commandScheduler;

    private Server() {
        System.setProperty("log4j.configurationFile", "src/main/resources/log4j.xml");
        serverLogger = LogManager.getLogger("Server.Server");

        serverLogger.info("New chat server is created");
        commands = PluginLoading.getPlugins();
        serverLogger.debug("Plugins are loaded");
        commandScheduler = new CommandScheduler(commands, this);
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                try {
                    new Connection(serverSocket.accept(), this);
                } catch (IOException e) {
                    serverLogger.error("Error while creating connection", e);
                }
            }
        } catch (IOException e) {
            serverLogger.error("Error while creating socket", e);
            throw new RuntimeException(e);

        }
    }

    public static void main(String[] args) {
        new Server();
    }

    synchronized private void computeCommand(Connection connection, String command) {
        commandScheduler.addTask(command, connection);
        serverLogger.debug("Added new command (" + command + ") to the Scheduler");
    }

    synchronized void onCommandFinished(Connection connection, String result) {
        serverLogger.debug("Command computation finished with result: " + result);
        sendTo(connection, result);
    }

    private boolean isCommand(String message) {
        if (!message.contains("/")) return false;
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

    private synchronized void notifyAll(String msg) {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        for (Connection connection : connections) {
            threadPool.submit(() -> connection.sendMessage(msg));
        }
        serverLogger.debug("Message is sent to all connections: " + msg);
        serverLogger.info("Message is sent to everybody");
    }

    private void sendTo(Connection connection, String msg) {
        connection.sendMessage(msg);
        serverLogger.debug("Command result (" + msg + ") is sent to " + connection.toString());
        serverLogger.info("Command result is sent");
    }
}
