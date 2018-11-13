import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Server implements ConnectionListener {
    private static Logger serverLogger;

    private final ArrayList<Connection> connections = new ArrayList<>();

    private Server() {
        System.out.println("Server running...");
        System.setProperty("log4j.configurationFile", "Server/src/main/resources/log4j.xml");
        serverLogger = LogManager.getLogger(Server.class);
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

    @Override
    public synchronized void onConnection(Connection connection) {
        connections.add(connection);
        notifyAll("New member joined the chat");
        serverLogger.info("Client connected: " + connection);
    }

    @Override
    public synchronized void onMessage(Connection connection, String message) {
        notifyAll(message);
    }

    @Override
    public synchronized void onDisconnect(Connection connection) {
        connections.remove(connection);
        notifyAll("One of the members left the chat");
        serverLogger.info("Client disconnected: " + connection);
    }

    @Override
    public synchronized void onException(Connection connection, Exception e) {
        System.out.println("Connection exception: " + e);
        serverLogger.error(e);
    }

    private void notifyAll(String msg) {
        //System.out.println(msg);
        for (Connection connection : connections) {
            connection.sendMessage(msg);
        }
        serverLogger.info("Message is sent: " + msg);
    }
}
