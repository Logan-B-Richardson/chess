package client;

import model.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Repl {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade server;

    private boolean loggedIn = false;
    private String authToken = null;
    private String username = null;
    private List<GameData> lastListedGames = new ArrayList<>();


}
