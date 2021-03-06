import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Connection {

    private final BufferedReader in;
    private final BufferedWriter out;
    private final Socket socket;

    private final Thread rwThread;
    private final ConnectionListener connectionListener;

    private static Logger connectionLogger;

    Connection(ConnectionListener connectionListener, String ip, int port) throws IOException {
        this(new Socket(ip, port), connectionListener);
    }

    Connection(Socket socket, ConnectionListener connectionListener) throws IOException {
        connectionLogger = LogManager.getLogger("Connection.Connection");

        this.socket = socket;
        this.connectionListener = connectionListener;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rwThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connectionLogger.info("Created new read/write " + this.toString() + " thread with " + connectionListener.getClass() + " as listener");
                    connectionListener.onConnection(Connection.this);
                    while (!rwThread.isInterrupted()) {
                        connectionLogger.debug("Read/write thread is interrupted");
                        connectionListener.onMessage(Connection.this, in.readLine());
                    }
                } catch (IOException e) {
                    connectionLogger.error("Error while creating connection", e);
                    e.printStackTrace();
                } finally {
                    connectionListener.onDisconnect(Connection.this);
                }
            }
        });
        rwThread.start();
    }

    synchronized void sendMessage(String msg) {
        try {
            out.write(msg + "\r\n");
            out.flush();
            connectionLogger.debug(msg + " is written to the buffer");
        } catch (IOException e) {
            connectionListener.onException(this, e);
            connectionLogger.error("Error while writing to the buffer", e);
            disconnect();
        }
    }

    synchronized void disconnect() {
        connectionLogger.debug("Trying to close " + socket.toString());
        rwThread.interrupt();
        try {
            socket.close();
            connectionLogger.info(socket.toString() + " closed.");
        } catch (IOException e) {
            connectionListener.onException(this, e);
            connectionLogger.error("Error while closing the socket", e);
        }

    }

    @Override
    public String toString() {
        return "IP " + socket.getInetAddress() + ", PORT " + socket.getPort();
    }
}
