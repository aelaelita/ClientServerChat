public interface ConnectionListener {
    void onConnection(Connection connection);
    void onMessage(Connection connection, String message);
    void onDisconnect(Connection connection);
    void onException(Connection connection, Exception e);
}
