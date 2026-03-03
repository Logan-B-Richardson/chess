package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.*;

public class ListGamesHandler {
    private final GameService service;
    private final Gson gson;

    public ListGamesHandler(GameService service, Gson gson) {
        this.service = service;
        this.gson = gson;
    }

    public void handle(Context context) {
        context.status(200).result(gson.toJson(service.listGames(context.header("authorization"))));
    }
}
