package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.JoinGameRequest;

public class JoinGameHandler {
    private final GameService service;
    private final Gson gson;

    public JoinGameHandler(GameService service, Gson gson) {
        this.service = service;
        this.gson = gson;
    }

    public void handle(Context context) {
        String token = context.header("authorization");
        JoinGameRequest request = gson.fromJson(context.body(), JoinGameRequest.class);
        service.joinGame(token, request);
        context.status(200).result("{}");
    }
}
