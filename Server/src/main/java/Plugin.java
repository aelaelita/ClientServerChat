public interface Plugin {
    boolean isCommand(String message);
    String getResult(String args);
}
