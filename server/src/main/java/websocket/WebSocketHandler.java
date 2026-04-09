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
    private static final Map<Session, ConnectionData> SESSIONS = new ConcurrentHashMap<>();
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
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
                default -> sendError(session, "Unsupported command");
            }
        } catch (Exception e) {
            sendError(session, "Invalid command");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        removeSession(session);
        System.out.println("Disconnected");
    }

    private void broadcast(int gameID, String message, Session exclude) {
        for (var entry : SESSIONS.entrySet()) {
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
            SESSIONS.put(session, new ConnectionData(username, gameID));
            LoadGameMessage loadGameMessage = new LoadGameMessage(gameData.game());
            session.getRemote().sendString(gson.toJson(loadGameMessage));
            String role;
            if (username.equals(gameData.whiteusername())) {
                role = "as white";
            } else if (username.equals(gameData.blackusername())) {
                role = "as black";
            } else {
                role = "as an observer";
            }
            NotificationMessage note = new NotificationMessage(username + " connected " + role);
            broadcast(gameID, gson.toJson(note), session);
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
            if (isGameOver(gameData)) {
                sendError(session, "Error: game is already over");
                return;
            }
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
                if (game.isInCheckmate(chess.ChessGame.TeamColor.WHITE) ||
                        game.isInCheckmate(chess.ChessGame.TeamColor.BLACK)) {
                    NotificationMessage note = new NotificationMessage("Checkmate!");
                    broadcast(gameID, gson.toJson(note), null);
                } else if (game.isInStalemate(chess.ChessGame.TeamColor.WHITE) ||
                        game.isInStalemate(chess.ChessGame.TeamColor.BLACK)) {
                    NotificationMessage note = new NotificationMessage("Stalemate!");
                    broadcast(gameID, gson.toJson(note), null);
                } else if (game.isInCheck(chess.ChessGame.TeamColor.WHITE) ||
                        game.isInCheck(chess.ChessGame.TeamColor.BLACK)) {
                    NotificationMessage note = new NotificationMessage("Check!");
                    broadcast(gameID, gson.toJson(note), null);
                }
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
            broadcastToGame(gameID, loadJson);
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

    private void handleLeave(Session session, UserGameCommand command) {
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
            boolean isWhite = username.equals(gameData.whiteusername());
            boolean isBlack = username.equals(gameData.blackusername());
            GameData updatedGame = gameData;
            if (isWhite) {
                updatedGame = new GameData(
                        gameID,
                        null,
                        gameData.blackusername(),
                        gameData.gamename(),
                        gameData.game()
                );
                dao.updateGame(updatedGame);
            } else if (isBlack) {
                updatedGame = new GameData(
                        gameID,
                        gameData.whiteusername(),
                        null,
                        gameData.gamename(),
                        gameData.game()
                );
                dao.updateGame(updatedGame);
            }
            removeSession(session);
            NotificationMessage note = new NotificationMessage(username + " left the game");
            broadcast(gameID, gson.toJson(note), session);
        } catch (Exception e) {
            sendError(session, "Error: failed to leave game");
        }
    }

    private void handleResign(Session session, UserGameCommand command) {
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
            boolean isWhite = username.equals(gameData.whiteusername());
            boolean isBlack = username.equals(gameData.blackusername());
            if (!isWhite && !isBlack) {
                sendError(session, "Error: observers cannot resign");
                return;
            }
            var game = gameData.game();
            if (isGameOver(gameData)) {
                sendError(session, "Error: game is already over");
                return;
            }
            GameData updatedGame = new GameData(
                    gameID,
                    gameData.whiteusername(),
                    gameData.blackusername(),
                    gameData.gamename() + "_OVER",
                    game
            );
            dao.updateGame(updatedGame);
            NotificationMessage note = new NotificationMessage(username + " resigned");
            String json = gson.toJson(note);
            broadcastToGame(gameID, json);
        } catch (Exception e) {
            sendError(session, "Error: failed to resign");
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

    private void removeSession(Session session) {
        SESSIONS.remove(session);
    }

    private boolean isGameOver(GameData gameData) {
        var game = gameData.game();
        var name = gameData.gamename();
        return game.isInCheckmate(chess.ChessGame.TeamColor.WHITE) ||
                game.isInCheckmate(chess.ChessGame.TeamColor.BLACK) ||
                game.isInStalemate(chess.ChessGame.TeamColor.WHITE) ||
                game.isInStalemate(chess.ChessGame.TeamColor.BLACK) ||
                (name != null && name.endsWith("_OVER"));
    }

    private void broadcastToGame(int gameID, String message) {
        for (Session s : SESSIONS.keySet()) {
            ConnectionData data = SESSIONS.get(s);
            if (data.gameID() == gameID && s.isOpen()) {
                try {
                    s.getRemote().sendString(message);
                } catch (IOException ignored) {}
            }
        }
    }
}
