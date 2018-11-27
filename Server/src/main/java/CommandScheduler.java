import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class CommandScheduler {
    private final ArrayList<Plugin> commands;
    private final Server listener;
    private ExecutorService threadPool;
    private static Logger serverLogger;
    static boolean isRunning;

    CommandScheduler(ArrayList<Plugin> commands, Server listener) {
        this.commands = commands;
        this.listener = listener;
        threadPool = Executors.newFixedThreadPool(8);
        serverLogger = LogManager.getLogger("Server.Server");
        isRunning = false;
    }

    void addTask(String task, Connection source) {
        CommandRunner commandRunner = new CommandRunner(commands, task, this, source);
        threadPool.submit(commandRunner);
        serverLogger.debug("Add new command task " + task + " from " + source.toString());
    }

    void onCommandFinished(String result, Connection connection) {
        serverLogger.debug("Send result of the command (" + result + ") to the server");
        listener.onCommandFinished(connection, result);
    }
}
