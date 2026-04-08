package client;

public interface WebSocketListener {
    void onLoadGame(chess.ChessGame game);
    void onError(String message);
    void onNotification(String message);
}
