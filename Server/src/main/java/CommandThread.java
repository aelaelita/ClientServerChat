import java.util.ArrayList;
import java.util.SplittableRandom;

class CommandThread extends Thread {
    private final ArrayList<Plugin> commands;
    private final String message;
    private String result;
    private Server listener;
    private Connection connection;


    CommandThread(ArrayList<Plugin> commands, String message, Server listener, Connection connection) {
        this.commands = commands;
        this.message = message;
        this.result = null;
        this.listener = listener;
        this.connection = connection;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for (Plugin command : commands) {
                if (command.isCommand(message)) {
                    result = command.getResult(message);
                    Thread.currentThread().interrupt();
                }
            }
            Thread.currentThread().interrupt();
        }
        listener.onCommandFinished(this, connection);
    }

    public String getResult() {
        return result;
    }
}
