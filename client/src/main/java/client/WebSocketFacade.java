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
        System.out.println("WS message: " + message);
    }

}
