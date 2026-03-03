package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;
import service.CreateGameRequest;
import service.CreateGameResult;

public class CreateGameHandler {
    private final GameService service;
    private final Gson gson;

    public CreateGameHandler(GameService service, Gson gson) {
        this.service = service;
        this.gson = gson;
    }

    public void handle(Context context) {
        context.status(200).result(gson.toJson(service.createGame(context.header("authorization"), gson.fromJson(context.body(), CreateGameRequest.class))));
    }
}
