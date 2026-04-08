package client;

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
    private Session session;
    private WebSocketListener listener;

    public WebSocketFacade(WebSocketListener listener) {
        this.listener = listener;
    }

    public void connect(String authToken, int gameID) throws Exception {
        if (session != null && session.isOpen()) {
            return;
        }
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, URI.create("ws://localhost:8080/ws"));
        UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        String json = gson.toJson(connectCommand);
        session.getBasicRemote().sendText(json);
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
}
