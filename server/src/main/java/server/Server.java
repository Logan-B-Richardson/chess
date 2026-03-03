package server;

import io.javalin.*;
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

        // exceptions
        javalin.exception(BadRequestException.class, (e, context) -> {
            context.status(400).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });
        javalin.exception(UnauthorizedException.class, (e, context) -> {
            context.status(401).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });
        javalin.exception(AlreadyTakenException.class , (e, context) -> {
            context.status(402).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });
        javalin.exception(ForbiddenException.class, (e, context) -> {
            context.status(403).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });
        javalin.exception(Exception.class, (e, context) -> {
            context.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
