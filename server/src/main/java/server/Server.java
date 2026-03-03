package server;

import io.javalin.*;
import io.javalin.http.Context;
import com.google.gson.Gson;
import dataaccess.*;
import handler.*;
import service.*;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // handler and service setup
        DataAccess dao = new DataAccessMemory();
        UserService userService = new UserService(dao);
        GameService gameService = new GameService(dao);
        ClearService clearService = new ClearService(dao);
        RegisterHandler registerHandler = new RegisterHandler(userService, gson);
        LoginHandler loginHandler = new LoginHandler(userService, gson);
        LogoutHandler logoutHandler = new LogoutHandler(userService);
        ListGamesHandler listGamesHandler = new ListGamesHandler(gameService, gson);
        CreateGameHandler createGameHandler = new CreateGameHandler(gameService, gson);
        JoinGameHandler joinGameHandler = new JoinGameHandler(gameService, gson);
        ClearHandler clearHandler = new ClearHandler(clearService);

        // url routes
        javalin.delete("/db", clearHandler::handle);
        javalin.post("/user", registerHandler::handle);
        javalin.post("/session", loginHandler::handle);
        javalin.delete("/session", logoutHandler::handle);
        javalin.get("/game", listGamesHandler::handle);
        javalin.post("/game", createGameHandler::handle);
        javalin.put("/game", joinGameHandler::handle);

        // exception handling
        javalin.exception(BadRequestException.class, (e, ctx) -> jsonError(ctx, 400, e));
        javalin.exception(UnauthorizedException.class, (e, ctx) -> jsonError(ctx, 401, e));
        javalin.exception(ForbiddenException.class, (e, ctx) -> jsonError(ctx, 403, e));
        javalin.exception(AlreadyTakenException.class, (e, ctx) -> jsonError(ctx, 403, e));
        javalin.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("message", "Error: internal server error")));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void jsonError(Context ctx, int status, Exception e) {
        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
    }
}
