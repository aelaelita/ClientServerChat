import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;


class CommandThread extends Thread {

    private final ArrayList<Plugin> commands;
    private final String message;
    private String result;
    private Server listener;
    private Connection connection;

    private static Logger serverLogger;

    CommandThread(ArrayList<Plugin> commands, String message, Server listener, Connection connection) {
        this.commands = commands;
        this.message = message;
        this.result = null;
        this.listener = listener;
        this.connection = connection;

        System.setProperty("log4j.configurationFile", "Server/src/main/resources/log4j.xml");
        serverLogger = LogManager.getLogger("Server.Server");
    }

    @Override
    public void run() {
        serverLogger.debug("Created new thread for command computation");
        while (!Thread.currentThread().isInterrupted()) {
            for (Plugin command : commands) {
                if (command.isCommand(message)) {
                    result = command.getResult(message);
                    Thread.currentThread().interrupt();
                }
            }
            Thread.currentThread().interrupt();
        }
        serverLogger.debug("Thread for command computation interrupted");
        listener.onCommandFinished(this, connection);
    }

    public String getResult() {
        return result;
    }
}
