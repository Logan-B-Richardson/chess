package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.*;

public class LoginHandler {
    private final UserService service;
    private final Gson gson;

    public LoginHandler(UserService service, Gson gson) {
        this.service = service;
        this.gson = gson;
    }

    public void handle(Context context) {
        LoginRequest request = gson.fromJson(context.body(), LoginRequest.class);
        LoginResult result = service.login(request);
        context.status(200).result(gson.toJson(result));
    }
}
