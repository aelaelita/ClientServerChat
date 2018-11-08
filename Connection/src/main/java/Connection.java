import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class Connection {
    private final Socket socket;
    private final Thread rxThread;
    private final ConnectionListener connectionListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    Connection(ConnectionListener connectionListener, String ip, int port) throws IOException {
        this(new Socket(ip, port), connectionListener);
    }

    Connection(Socket socket, ConnectionListener connectionListener) throws IOException {
        this.socket = socket;
        this.connectionListener = connectionListener;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connectionListener.onConnectionReady(Connection.this);
                    while (!rxThread.isInterrupted()) {
                        connectionListener.onReceiveMessage(Connection.this, in.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connectionListener.onDisconnect(Connection.this);
                }
            }
        });
        rxThread.start();
    }

     synchronized void sendMessage(String msg) {
        try {
            out.write(msg + "\r\n");//символ конца строки
            out.flush();
        } catch (IOException e) {
            connectionListener.onException(this, e);
            disconnect();
        }
    }

     synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            connectionListener.onException(this, e);
        }
    }

    @Override
    public String toString() {
        return "Connection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
