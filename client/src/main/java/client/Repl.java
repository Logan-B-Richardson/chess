package client;

import service.records.GameSummary;

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
    private List<GameSummary> lastListedGames = new ArrayList<>();

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
                    System.out.print("[LOGGED_IN] >>> ");
                    String input = scanner.nextLine().trim();
                    handlePostlogin(input);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private boolean handlePrelogin(String input) {
        String command = input.toLowerCase();
        switch (command) {
            case "help" -> printPreloginHelp();
            case "quit" -> {
                System.out.println("Goodbye.");
                return true;
            }
            case "login" -> login();
            case "register" -> register();
            default -> System.out.println("Unknown command. Type help.");
        }
        return false;
    }

    private void handlePostlogin(String input) {
        String command = input.toLowerCase();
        switch (command) {
            case "help" -> printPostloginHelp();
            case "logout" -> logout();
            case "create game" -> createGame();
            case "list games" -> listGames();
            case "play game" -> playGame();
            case "observe game" -> observeGame();
            default -> System.out.println("Unknown comman. Type help.");
        }
    }

    private void printPreloginHelp() {
        System.out.println("help - show commands");
        System.out.println("register - create account");
        System.out.println("login - sign in");
        System.out.println("quit - exit");
    }

    private void printPostloginHelp() {
        System.out.println("help - show commands");
        System.out.println("logout - sign out");
        System.out.println("create game - create a game");
        System.out.println("list games - list games");
        System.out.println("play game - join a game");
        System.out.println("observe game - observe a game");
    }

    private void login() {
        System.out.println("TODO");
    }

    private void register() {
        try {
            System.out.println("username: ");
            String username = scanner.nextLine().trim();
            System.out.print("password: ");
            String password = scanner.nextLine().trim();
            System.out.print("email: ");
            String email = scanner.nextLine().trim();
            var auth = server.register(username, password, email);
            this.loggedIn = true;
            this.authToken = auth.authToken();
            this.username = auth.username();
            System.out.println("Registered and logged in as " + username);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void logout() {
        System.out.println("TODO");
    }

    private void createGame() {
        System.out.println("TODO");
    }

    private void listGames() {
        System.out.println("TODO");
    }

    private void playGame() {
        System.out.println("TODO");
    }

    private void observeGame() {
        System.out.println("TODO");
    }
}
