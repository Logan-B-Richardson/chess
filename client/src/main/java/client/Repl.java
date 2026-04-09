package client;

import service.records.GameSummary;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPiece.PieceType;
import chess.ChessPosition;
import ui.BoardUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Collection;
import java.util.HashSet;

public class Repl {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade server;
    private final WebSocketFacade webSocketClient;

    // internal flags
    private boolean loggedIn = false;
    private String authToken = null;
    private String username = null;
    private List<GameSummary> lastListedGames = new ArrayList<>();
    private ChessGame.TeamColor perspective = ChessGame.TeamColor.WHITE;
    private boolean inGameplay = false;
    private int currentGameID = -1;
    private ChessGame currentGame = null;
    private boolean observerMode = false;

    public Repl(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
        this.webSocketClient = new WebSocketFacade(serverUrl, new WebSocketListener() {
            @Override
            public void onLoadGame(ChessGame game) {
                currentGame = game;
                BoardUI.drawBoard(game, perspective);
            }

            @Override
            public void onError(String message) {
                System.out.println(message);
            }

            @Override
            public void onNotification(String message) {
                System.out.println(message);
            }
        });
    }

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
                } else if (inGameplay) {
                    System.out.print("[GAMEPLAY] >>> ");
                    String input = scanner.nextLine().trim();
                    handleGameplay(input);
                } else {
                    System.out.print("[LOGGED_IN] >>> ");
                    String input = scanner.nextLine().trim();
                    handlePostlogin(input);
                }
            } catch (Exception e) {
                System.out.println(friendlyMessage(e));
            }
        }
    }

    private boolean handlePrelogin(String input) {
        if (input.isBlank()) {
            System.out.println("Unknown command. Type help.");
            return false;
        }
        String[] tokens = input.trim().split("\\s+");
        String command = tokens[0].toLowerCase();
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
        if (input.isBlank()) {
            System.out.println("Unknown command. Type help.");
            return;
        }
        String[] tokens = input.trim().split("\\s+");
        String command = tokens[0].toLowerCase();
        switch (command) {
            case "create" -> createGame(tokens);
            case "list" -> listGames();
            case "play" -> playGame(tokens);
            case "observe" -> observeGame(tokens);
            case "logout" -> logout();
            case "help" -> printPostloginHelp();
            default -> System.out.println("Unknown command. Type help.");
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
        System.out.println("create <game name> - create a game");
        System.out.println("list - list games");
        System.out.println("play <game number> <WHITE|BLACK> - join a game");
        System.out.println("observe <game number> - observe a game");
    }

    private void login() {
        try {
            System.out.print("username: ");
            String username = scanner.nextLine().trim();
            System.out.print("password: ");
            String password = scanner.nextLine().trim();
            var auth = server.login(username, password);
            this.loggedIn = true;
            this.authToken = auth.authToken();
            this.username = auth.username();
            System.out.println("Logged in as " + this.username);
            lastListedGames.clear();
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
        }
    }

    private void register() {
        try {
            System.out.print("username: ");
            String username = scanner.nextLine().trim();
            System.out.print("password: ");
            String password = scanner.nextLine().trim();
            System.out.print("email: ");
            String email = scanner.nextLine().trim();
            var auth = server.register(username, password, email);
            this.loggedIn = true;
            this.authToken = auth.authToken();
            this.username = auth.username();
            System.out.println("Registered and logged in as " + this.username);
            lastListedGames.clear();
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
        }
    }

    private void logout() {
        try {
            server.logout(authToken);
            loggedIn = false;
            authToken = null;
            username = null;
            lastListedGames.clear();
            System.out.println("Logged out.");
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
        }
    }

    private void createGame(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: create <game name>");
            return;
        }
        String gameName = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
        try {
            server.createGame(authToken, gameName);
            System.out.println("Game created.");
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
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
            System.out.println(friendlyMessage(e));
        }
    }

    private void playGame(String[] tokens) {
        try {
            if (lastListedGames.isEmpty()) {
                lastListedGames = server.listGames(authToken);
            }
            if (tokens.length != 3) {
                System.out.println("Usage: play <game number> <WHITE|BLACK>");
                return;
            }
            int num = Integer.parseInt(tokens[1]);
            String color = tokens[2].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("Color must be WHITE or BLACK.");
                return;
            }
            if (num < 1 || num > lastListedGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }
            int gameID = lastListedGames.get(num - 1).gameID();
            server.joinGame(authToken, color, gameID);
            currentGameID = gameID;
            observerMode = false;
            perspective = color.equals("BLACK") ?
                    ChessGame.TeamColor.BLACK :
                    ChessGame.TeamColor.WHITE;
            webSocketClient.connect(authToken, gameID);
            inGameplay = true;
            System.out.println("Joined game. Waiting for server...");
        } catch (NumberFormatException e) {
            System.out.println("Game number must be a number.");
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
        }
    }

    private void observeGame(String[] tokens) {
        try {
            if (lastListedGames.isEmpty()) {
                lastListedGames = server.listGames(authToken);
            }
            if (tokens.length != 2) {
                System.out.println("Usage: observe <game number>");
                return;
            }
            int num = Integer.parseInt(tokens[1]);
            if (num < 1 || num > lastListedGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }
            int gameID = lastListedGames.get(num - 1).gameID();
            currentGameID = gameID;
            observerMode = true;
            perspective = ChessGame.TeamColor.WHITE;
            webSocketClient.connect(authToken, gameID);
            inGameplay = true;
            System.out.println("Joined game. Waiting for server...");
        } catch (NumberFormatException e) {
            System.out.println("Game number must be a number.");
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
        }
    }


    private String friendlyMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return "Something went wrong.";
        }
        msg = msg.toLowerCase();
        if (msg.contains("connection refused")) {
            return "Could not connect to the server. Make sure the server is running.";
        }
        if (msg.contains("unauthorized")) {
            return "You are not authorized to do that.";
        }
        if (msg.contains("already taken")) {
            return "That username or game is already taken.";
        }
        if (msg.contains("bad request")) {
            return "That command could not be completed.";
        }
        if (msg.contains("invalid position")) {
            return "Invalid position. Use format like e2.";
        }
        if (msg.contains("promotion")) {
            return "Invalid promotion piece. Use Q, R, B, or N.";
        }
        return msg;
    }

    private void handleGameplay(String input) {
        if (input.isBlank()) {
            System.out.println("Unknown command. Type help.");
            return;
        }
        String[] tokens = input.trim().split("\\s+");
        String command = tokens[0].toLowerCase();
        switch (command) {
            case "help" -> printGameplayHelp();
            case "redraw" -> redrawBoard();
            case "highlight" -> highlightMoves();
            case "move" -> makeMove();
            case "leave" -> leaveGame();
            case "resign" -> resignGame();
            default -> System.out.println("Unknown command. Type help.");
        }
    }

    private void printGameplayHelp() {
        System.out.println("help - show gameplay commands");
        System.out.println("redraw - redraw the chess board");
        System.out.println("highlight - highlight legal moves for a piece");
        System.out.println("move - make a move");
        System.out.println("leave - leave the game");
        System.out.println("resign - resign the game");
    }

    private void redrawBoard() {
        if (currentGame == null) {
            System.out.println("No game loaded yet.");
            return;
        }
        BoardUI.drawBoard(currentGame, perspective);
    }

    private void leaveGame() {
        try {
            webSocketClient.leave(authToken, currentGameID);
            webSocketClient.close();
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
        }
        inGameplay = false;
        currentGameID = -1;
        currentGame = null;
        observerMode = false;
        perspective = ChessGame.TeamColor.WHITE;
        System.out.println("Left game.");
    }

    private void resignGame() {
        if (observerMode) {
            System.out.println("Observers cannot resign.");
            return;
        }
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (!answer.equals("yes")) {
            System.out.println("Resign cancelled.");
            return;
        }
        try {
            webSocketClient.resign(authToken, currentGameID);
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
        }
    }

    private void makeMove() {
        if (observerMode) {
            System.out.println("Observers cannot make moves.");
            return;
        }
        if (currentGame == null) {
            System.out.println("No game loaded yet.");
            return;
        }
        try {
            System.out.print("Enter start position (e.g. e2): ");
            ChessPosition start = parsePosition(scanner.nextLine().trim());
            System.out.print("Enter end position (e.g. e4): ");
            ChessPosition end = parsePosition(scanner.nextLine().trim());
            ChessPiece piece = currentGame.getBoard().getPiece(start);
            if (piece == null) {
                System.out.println("No piece at that starting position.");
                return;
            }
            PieceType promotionPiece = null;
            if (isPromotionMove(piece, end)) {
                System.out.print("Promote to (Q, R, B, N): ");
                promotionPiece = parsePromotionPiece(scanner.nextLine().trim());
            }
            ChessMove move = new ChessMove(start, end, promotionPiece);
            if (!webSocketClient.isConnected()) {
                System.out.println("Reconnecting websocket...");
                webSocketClient.connect(authToken, currentGameID);
            }
            webSocketClient.makeMove(authToken, currentGameID, move);
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
        }
    }

    private void highlightMoves() {
        if (currentGame == null) {
            System.out.println("No game loaded yet.");
            return;
        }
        try {
            System.out.print("Enter piece position (e.g. e2): ");
            String input = scanner.nextLine().trim();
            ChessPosition start = parsePosition(input);
            ChessPiece piece = currentGame.getBoard().getPiece(start);
            if (piece == null) {
                System.out.println("No piece at that position.");
                return;
            }
            Collection<ChessMove> legalMoves = currentGame.validMoves(start);
            Collection<ChessPosition> highlights = new HashSet<>();
            highlights.add(start);
            if (legalMoves != null) {
                for (ChessMove move : legalMoves) {
                    highlights.add(move.getEndPosition());
                }
            }
            BoardUI.drawBoard(currentGame, perspective, highlights);
        } catch (Exception e) {
            System.out.println(friendlyMessage(e));
        }
    }

    private ChessPosition parsePosition(String input) {
        input = input.trim().toLowerCase();
        if (input.length() != 2) {
            throw new IllegalArgumentException("Invalid position. Use format like e2.");
        }
        char file = input.charAt(0);
        char rank = input.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid position. Use format like e2.");
        }
        int col = file - 'a' + 1;
        int row = rank - '0';
        return new ChessPosition(row, col);
    }

    private PieceType parsePromotionPiece(String input) {
        input = input.trim().toUpperCase();
        return switch (input) {
            case "Q", "QUEEN" -> PieceType.QUEEN;
            case "R", "ROOK" -> PieceType.ROOK;
            case "B", "BISHOP" -> PieceType.BISHOP;
            case "N", "KNIGHT" -> PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece. Use Q, R, B, or N.");
        };
    }

    private boolean isPromotionMove(ChessPiece piece, ChessPosition endPosition) {
        if (piece == null || piece.getPieceType() != PieceType.PAWN) {
            return false;
        }
        return (piece.getTeamColor() == ChessGame.TeamColor.WHITE && endPosition.getRow() == 8) ||
                (piece.getTeamColor() == ChessGame.TeamColor.BLACK && endPosition.getRow() == 1);
    }
}
