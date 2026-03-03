package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.UserService;
import service.RegisterRequest;
import service.RegisterResult;

public class RegisterHandler {
    private final UserService service;
    private final Gson gson;

    public RegisterHandler(UserService service, Gson gson) {
        this.service = service;
        this.gson = gson;
    }

    public void handle(Context context) {
        context.status(200).result(gson.toJson(service.register(gson.fromJson(context.body(), RegisterRequest.class))));
    }
}
