package server;

import io.javalin.*;
import com.google.gson.Gson;
import dataaccess.*;
import handler.*;
import service.*;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        DataAccess dao = new DataAccessMemory();
        UserService userService = new UserService(dao);
        GameService gameService = new GameService(dao);
        ClearService clearService = new ClearService(dao);
        RegisterHandler registerHandler = new RegisterHandler(userService, gson);
        LoginHandler loginHandler = new LoginHandler(userService, gson);
        LogoutHandler logoutHandler = new LogoutHandler(userService);
        CreateGameHandler createGameHandler = new CreateGameHandler(gameService, gson);
        JoinGameHandler joinGameHandler = new JoinGameHandler(gameService, gson);
        ClearHandler clearHandler = new ClearHandler(clearService);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
