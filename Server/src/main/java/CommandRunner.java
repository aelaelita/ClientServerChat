import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.Callable;


class CommandRunner implements Callable<String> {

    private final ArrayList<Plugin> commands;
    private final String message;
    private static Logger serverLogger;

    CommandRunner(ArrayList<Plugin> commands, String message) {
        this.commands = commands;
        this.message = message;
        serverLogger = LogManager.getLogger("Server.Server");
    }

    @Override
    public String call() throws Exception {
        serverLogger.debug("Running callable for " + message + " computation");
        String result = null;
        for (Plugin command : commands) {
            if (command.isCommand(message)) {
                result = command.getResult(message);
            }
        }
        return result;
    }
}
