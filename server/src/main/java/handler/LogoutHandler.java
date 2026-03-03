package handler;

import io.javalin.http.Context;
import service.UserService;

public class LogoutHandler {
    private final UserService service;

    public LogoutHandler(UserService service) {
        this.service = service;
    }

    public void handle(Context context) {
        service.logout(context.header("authorization"));
        context.status(200).result("{}");
    }
}
