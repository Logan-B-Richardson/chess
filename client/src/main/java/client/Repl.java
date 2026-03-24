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
        try {
            System.out.println("username: ");
            String username = scanner.nextLine().trim();
            System.out.print("password: ");
            String password = scanner.nextLine().trim();
            var auth = server.login(username, password);
            this.loggedIn = true;
            this.authToken = auth.authToken();
            this.username = auth.username();
            System.out.println("Logged in as " + username);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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
        try {
            server.logout(authToken);
            loggedIn = false;
            authToken = null;
            username = null;
            lastListedGames.clear();
            System.out.println("logged out.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void createGame() {
        try {
            System.out.println("game name: ");
            String gameName = scanner.nextLine().trim();
            if (gameName.isEmpty()) {
                System.out.println("Game name cannot be empty.");
                return;
            }
            server.createGame(authToken, gameName);
            System.out.println("Game created.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void listGames() {
        try {
            lastListedGames = server.listGames(authToken);
            if (lastListedGames.isEmpty()) {
                System.out.println("No games found.");
                return;
            }
            for (int i = 0; i < lastListedGames.size(); i++) {
                var game = lastListedGames.get(i);
                String white = game.whiteUsername() == null ? "<open>" : game.whiteUsername();
                String black = game.blackUsername() == null ? "<open>" : game.blackUsername();
                System.out.printf("%d. %s - White: %s, Black: %s%n", i+1, game.gameName(), white, black);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void playGame() {
        System.out.println("TODO");
    }

    private void observeGame() {
        System.out.println("TODO");
    }
}
