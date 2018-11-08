import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Client extends JFrame implements ActionListener, ConnectionListener {
    private static final String IP = "localhost";
    private static final int PORT = 8189;
    private static int WIDTH = 600;
    private static int HEIGHT = 400;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldNickname = new JTextField("nickname");
    private final JTextField fieldInput = new JTextField("");

    private Connection connection;

    private Client() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        log.setEditable(false);
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);

        fieldInput.addActionListener(this);
        add(fieldInput, BorderLayout.SOUTH);

        add(fieldNickname, BorderLayout.NORTH);

        setVisible(true);
        try {
            connection = new Connection(this, IP, PORT);
        } catch (IOException e) {
            printMessage("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if (msg.equals("")) return;
        fieldInput.setText("");
        connection.sendMessage(fieldNickname.getText() + ": " + msg);
    }

    @Override
    public void onConnectionReady(Connection connection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onReceiveMessage(Connection connection, String message) {
        printMessage(message);
    }

    @Override
    public void onDisconnect(Connection connection) {
        printMessage("Connection close");
    }

    @Override
    public void onException(Connection connection, Exception e) {
        printMessage("Connection exception: " + e);
    }

    private synchronized void printMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }
}
