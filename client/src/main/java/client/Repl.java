package client;

import model.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Repl {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade server;

    // internal flags
    private boolean loggedIn = false;
    private String authToken = null;
    private String username = null;
    private List<GameData> lastListedGames = new ArrayList<>();

    public void run() {
        System.out.println("Welcome to Chess. Type help to get started.");
        while (true) {
            try {
                if (!loggedIn) {
                    System.out.print("[LOGGED_OUT] >>> ");
                    String input = scanner.nextLine().trim();
                    if (handlePrelogin(input)) {
                        break;
                    }
                } else {
                    System.out.print("[LOGGED_OUT] >>> ");
                    String input = scanner.nextLine().trim();
                    handlePostlogin(input);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
