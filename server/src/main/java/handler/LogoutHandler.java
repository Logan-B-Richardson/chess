package handler;

import io.javalin.http.Context;
import service.UserService;

public class LogoutHandler {
    private final UserService service;

    public LogoutHandler(UserService service) {
        this.service = service;
    }

    public void handle(Context context) {
        String token = context.header("authorization");
        service.logout(token);
        context.status(200).result("{}");
    }
}
