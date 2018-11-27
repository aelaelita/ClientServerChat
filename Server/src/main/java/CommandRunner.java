import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.Callable;


class CommandRunner implements Runnable {

    private final ArrayList<Plugin> commands;
    private final String message;
    private String result;
    private CommandScheduler listener;
    private Connection connection;

    private static Logger serverLogger;

    CommandRunner(ArrayList<Plugin> commands, String message, CommandScheduler listener, Connection connection) {
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
        }
        serverLogger.debug("CommandRunner for computing " + message + " finished");
        listener.onCommandFinished(result, connection);
    }
}

