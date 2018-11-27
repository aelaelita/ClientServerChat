import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class CommandScheduler implements Runnable {
    private final ArrayList<Plugin> commands;
    private final Server listener;
    private ArrayList<Pair> tasks;
    private ExecutorService threadPool;
    private static Logger serverLogger;
    static boolean isRunning;

    CommandScheduler(ArrayList<Plugin> commands, Server listener) {
        this.commands = commands;
        this.listener = listener;
        threadPool = Executors.newFixedThreadPool(8);
        tasks = new ArrayList<>();
        serverLogger = LogManager.getLogger("Server.Server");
        isRunning = false;
    }

    void addTask(String task, Connection source) {
        CommandRunner commandRunner = new CommandRunner(commands, task);
        Future<String> future = threadPool.submit(commandRunner);
        tasks.add(new Pair(source, future));
        serverLogger.debug("Add new command task " + task + " from " + source.toString());
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && tasks.size() != 0) {
            for (Pair taskPair : tasks) {
                try {
                    isRunning = true;
                    if (taskPair.future.isDone()) {
                        listener.onCommandFinished(taskPair.source, taskPair.future.get());
                        tasks.remove(taskPair);
                        serverLogger.debug("Computation of command from " + taskPair.source + " is finished");
                    }
                } catch (ExecutionException e) {
                    Thread.currentThread().interrupt();
                    isRunning = false;
                    serverLogger.error("Error while getting result of the command", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    isRunning = false;
                    serverLogger.error("Error while getting result of the command", e);
                }
            }
        }
    }

    class Pair {
        Connection source;
        Future<String> future;

        public Pair(Connection source, Future<String> future) {
            this.source = source;
            this.future = future;
        }
    }
}
