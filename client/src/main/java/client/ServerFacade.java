package client;

import com.google.gson.Gson;
import model.AuthData;
import service.records.GameSummary;
import service.records.ListGameResults;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        return makeRequest("POST", "/user",
                new RegisterRequest(username, password, email),
                null,
                AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        return makeRequest("POST", "/session",
                new LoginRequest(username, password),
                null,
                AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", null, authToken, null);
    }

    public int createGame(String authToken, String gameName) throws Exception {
        CreateGameResponse response = makeRequest("POST", "/game",
                new CreateGameRequest(gameName),
                authToken,
                CreateGameResponse.class);
        return response.gameID();
    }

    public List<GameSummary> listGames(String authToken) throws Exception {
        ListGameResults response = makeRequest("GET", "/game",
                null,
                authToken,
                ListGameResults.class);
        return response.games();
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws Exception {
        makeRequest("PUT", "/game",
                new JoinGameRequest(playerColor, gameID),
                authToken,
                null);
    }

    private <T> T makeRequest(
            String method,
            String path,
            Object requestBody,
            String authToken,
            Class<T> responseClass
    ) throws Exception {
        URL url = URI.create(serverUrl + path).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            http.setRequestProperty("Authorization", authToken);
        }
        if (requestBody != null) {
            http.setDoOutput(true);
            try (OutputStream os = http.getOutputStream()) {
                os.write(gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8));
            }
        }
        int status = http.getResponseCode();
        if (status / 100 != 2) {
            InputStreamReader errorReader = new InputStreamReader(
                    http.getErrorStream(), StandardCharsets.UTF_8);
            ErrorResponse error = gson.fromJson(errorReader, ErrorResponse.class);
            if (error != null && error.message() != null) {
                throw new Exception(error.message());
            } else {
                throw new Exception("Request failed with status code " + status);
            }
        }
        if (responseClass == null) {
            return null;
        }
        try (InputStreamReader inputReader = new InputStreamReader(
                http.getInputStream(), StandardCharsets.UTF_8)) {
            return gson.fromJson(inputReader, responseClass);
        }
    }

    private record ErrorResponse(String message) {}

    public record RegisterRequest(String username, String password, String email) {}
    public record LoginRequest(String username, String password) {}
    public record CreateGameRequest(String gameName) {}
    public record JoinGameRequest(String playerColor, int gameID) {}
    public record CreateGameResponse(int gameID) {}
}
