package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;
import service.records.CreateGameRequest;
import service.records.CreateGameResult;

public class CreateGameHandler {
    private final GameService service;
    private final Gson gson;

    public CreateGameHandler(GameService service, Gson gson) {
        this.service = service;
        this.gson = gson;
    }

    public void handle(Context context) {
        String token = context.header("authorization");
        CreateGameRequest request = gson.fromJson(context.body(), CreateGameRequest.class);
        CreateGameResult result = service.createGame(token, request);
        context.status(200).result(gson.toJson(result));
    }
}
