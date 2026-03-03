package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.*;
import service.records.LoginRequest;

public class LoginHandler {
    private final UserService service;
    private final Gson gson;

    public LoginHandler(UserService service, Gson gson) {
        this.service = service;
        this.gson = gson;
    }

    public void handle(Context context) {
        context.status(200).result(gson.toJson(service.login(gson.fromJson(context.body(), LoginRequest.class))));
    }
}
