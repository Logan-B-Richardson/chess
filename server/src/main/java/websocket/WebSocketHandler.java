package websocket;

import com.google.gson.Gson;
import dataaccess.MySqlDataAccess;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.messages.NotificationMessage;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final Gson gson = new Gson();
    private final MySqlDataAccess dao = new MySqlDataAccess();
    private static final Map<Session, ConnectionData> sessions = new ConcurrentHashMap<>();
    private record ConnectionData(String username, int gameID) {}

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMakeMove(session, gson.fromJson(message, MakeMoveCommand.class));
                default -> sendError(session, "Unsupported command");
            }
        } catch (Exception e) {
            sendError(session, "Invalid command");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        sessions.remove(session);
        System.out.println("Disconnected");
    }

    private void broadcast(int gameID, String message, Session exclude) {
        for (var entry : sessions.entrySet()) {
            Session s = entry.getKey();
            ConnectionData data = entry.getValue();
            if (data.gameID() == gameID && s.isOpen() && s != exclude) {
                try {
                    s.getRemote().sendString(message);
                } catch (IOException ignored) {}
            }
        }
    }

    private void handleConnect(Session session, UserGameCommand command) {
        try {
            String authToken = command.getAuthToken();
            int gameID = command.getGameID();
            AuthData auth = dao.getAuth(authToken);
            if (auth == null) {
                sendError(session, "Error: unauthorized");
                return;
            }

            GameData gameData = dao.getGame(gameID);
            if (gameData == null) {
                sendError(session, "Error: game not found");
                return;
            }

            String username = auth.username();
            sessions.put(session, new ConnectionData(username, gameID));
            LoadGameMessage loadGameMessage = new LoadGameMessage(gameData.game());
            session.getRemote().sendString(gson.toJson(loadGameMessage));
            System.out.println("handleConnect called for game " + gameID);
        } catch (Exception e) {
            sendError(session, "Error: unable to connect to game");
        }
    }

    private void handleMakeMove(Session session, MakeMoveCommand command) {
        try {
            String authToken = command.getAuthToken();
            int gameID = command.getGameID();
            AuthData auth = dao.getAuth(authToken);
            if (auth == null) {
                sendError(session, "Error: unauthorized");
                return;
            }
            GameData gameData = dao.getGame(gameID);
            if (gameData == null) {
                sendError(session, "Error: game not found");
                return;
            }
            var game = gameData.game();
            var move = command.getMove();
            String username = auth.username();
            boolean isWhite = username.equals(gameData.whiteusername());
            boolean isBlack = username.equals(gameData.blackusername());
            if (!isWhite && !isBlack) {
                sendError(session, "Error: observers cannot move");
                return;
            }
            if ((game.getTeamTurn() == chess.ChessGame.TeamColor.WHITE && !isWhite) ||
                    (game.getTeamTurn() == chess.ChessGame.TeamColor.BLACK && !isBlack)) {
                sendError(session, "Error: not your turn");
                return;
            }
            try {
                game.makeMove(move);
            } catch (Exception e) {
                sendError(session, "Error: illegal move");
                return;
            }
            GameData updatedGame = new GameData(
                    gameID,
                    gameData.whiteusername(),
                    gameData.blackusername(),
                    gameData.gamename(),
                    game
            );
            dao.updateGame(updatedGame);
            LoadGameMessage loadMsg = new LoadGameMessage(game);
            String loadJson = gson.toJson(loadMsg);
            for (Session s : sessions.keySet()) {
                ConnectionData data = sessions.get(s);
                if (data.gameID() == gameID && s.isOpen()) {
                    s.getRemote().sendString(loadJson);
                }
            }
            NotificationMessage note = new NotificationMessage(
                    username + " moved " +
                            positionToString(move.getStartPosition()) +
                            " to " +
                            positionToString(move.getEndPosition())
            );
            broadcast(gameID, gson.toJson(note), session);
        } catch (Exception e) {
            sendError(session, "Error: failed to make move");
        }
    }

    private void sendError(Session session, String errorText) {
        try {
            ErrorMessage errorMessage = new ErrorMessage(errorText);
            session.getRemote().sendString(gson.toJson(errorMessage));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String positionToString(chess.ChessPosition pos) {
        char file = (char) ('a' + pos.getColumn() - 1);
        int rank = pos.getRow();
        return "" + file + rank;
    }
}
