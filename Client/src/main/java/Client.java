import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client extends JFrame implements ActionListener, ConnectionListener {
    private static final String IP = "localhost";
    private static final int PORT = 8080;

    private static Logger clientLogger;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }

    private final JTextArea chat = new JTextArea();
    private final JTextField nickname = new JTextField("Имя");
    private final JTextField input = new JTextField("");

    private Connection connection;

    private Client() {
        System.setProperty("log4j.configurationFile", "Client/src/main/resources/log4j.xml");
        clientLogger = LogManager.getLogger(Client.class);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        chat.setEditable(false);
        chat.setLineWrap(true);
        add(chat, BorderLayout.CENTER);

        input.addActionListener(this);
        add(input, BorderLayout.SOUTH);

        add(nickname, BorderLayout.NORTH);

        setVisible(true);
        try {
            connection = new Connection(this, IP, PORT);
        } catch (IOException e) {
            clientLogger.error(e);
            printMessage("Ошибка cоединения: " + e);
        }finally {
            clientLogger.info("Connection created "+connection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = input.getText();
        if (msg.equals("")) return;
        input.setText("");
        connection.sendMessage(nickname.getText() + ": " + msg);
        clientLogger.info("Message is sent to the connection: " + msg);
    }

    @Override
    public void onConnection(Connection connection) {
        printMessage(" Соединение установлено");
        clientLogger.info("Connection ready");
    }

    @Override
    public void onMessage(Connection connection, String message) {
        printMessage(message);
        clientLogger.info("Message is printed to the UI: " + message);
    }

    @Override
    public void onDisconnect(Connection connection) {
        printMessage("Соединение разорвано");
        clientLogger.info("Connection closed");
    }

    @Override
    public void onException(Connection connection, Exception e) {
        printMessage("Ошибка оединения: " + e);
        clientLogger.error(e);
    }


    private synchronized void printMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            chat.append(msg + "\n");
            chat.setCaretPosition(chat.getDocument().getLength());
        });
    }
}
