package client;

import chess.ChessMove;
import websocket.commands.MakeMoveCommand;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.CloseReason;

import com.google.gson.Gson;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import websocket.commands.UserGameCommand;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade {
    private final Gson gson = new Gson();
    private final String wsUrl;
    private Session session;
    private WebSocketListener listener;
    private int connectedGameID = -1;

    public WebSocketFacade(String serverUrl, WebSocketListener listener) {
        this.listener = listener;
        this.wsUrl = serverUrl.replaceFirst("^http", "ws") + "/ws";
    }

    public void connect(String authToken, int gameID) throws Exception {
        if (session != null && session.isOpen()) {
            System.out.println("WebSocket already open.");
            return;
        }
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        System.out.println("Connecting to " + wsUrl);
        session = container.connectToServer(this, URI.create(wsUrl));
        connectedGameID = gameID;
        System.out.println("WebSocket connected for game " + connectedGameID);
        System.out.println("Session open after connect: " + session.isOpen());
        UserGameCommand connectCommand =
                new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        String json = gson.toJson(connectCommand);
        session.getBasicRemote().sendText(json);
        System.out.println("CONNECT sent.");
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws Exception {
        System.out.println("Before move send: session=" + session +
                ", open=" + (session != null && session.isOpen()));
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("WebSocket is not connected.");
        }
        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        String json = gson.toJson(command);
        session.getBasicRemote().sendText(json);
    }

    public void leave(String authToken, int gameID) throws Exception {
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("WebSocket is not connected.");
        }
        UserGameCommand command =
                new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        String json = gson.toJson(command);
        session.getBasicRemote().sendText(json);
    }

    public void resign(String authToken, int gameID) throws Exception {
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("WebSocket is not connected.");
        }
        UserGameCommand command =
                new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        String json = gson.toJson(command);
        session.getBasicRemote().sendText(json);
    }

    public void close() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            var base = gson.fromJson(message, websocket.messages.ServerMessage.class);
            switch (base.getServerMessageType()) {
                case LOAD_GAME -> {
                    var loadMsg = gson.fromJson(message, websocket.messages.LoadGameMessage.class);
                    listener.onLoadGame(loadMsg.getGame());
                }
                case ERROR -> {
                    var err = gson.fromJson(message, websocket.messages.ErrorMessage.class);
                    listener.onError(err.getErrorMessage());
                }
                case NOTIFICATION -> {
                    var note = gson.fromJson(message, websocket.messages.NotificationMessage.class);
                    listener.onNotification(note.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to parse WS message: " + message);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket connected.");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
        this.session = null;
        this.connectedGameID = -1;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
        this.session = null;
        this.connectedGameID = -1;
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }
}
