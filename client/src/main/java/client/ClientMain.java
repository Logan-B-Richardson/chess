package client;

import chess.*;

public class ClientMain {
    static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        Repl repl = new Repl(serverUrl);
        repl.run();
    }
}
