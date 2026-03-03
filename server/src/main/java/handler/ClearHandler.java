package handler;

import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {
    private final ClearService service;

    public ClearHandler(ClearService service) {
        this.service = service;
    }

    public void handle(Context context) {
        service.clear();
        context.status(200).result("{}");
    }
}
