package client;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    private <T> T makeRequest(String method, String path, Object requestBody, String authToken, Class<T> responseClass) throws Exception {
        URL url = (URI.create(serverUrl + path)).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            http.setDoOutput(true);
            try (OutputStream os = http.getOutputStream()) {
                os.write(gson.toJson(requestBody).getBytes());
            }
        }
        int status = http.getResponseCode();
        if (status / 100 != 2) {
            ErrorResponse error = gson.fromJson(new InputStreamReader(http.getErrorStream()), ErrorResponse.class);
            throw new Exception(error.message());
        }
        if (responseClass == null) {
            return null;
        }
        return gson.fromJson(new InputStreamReader(http.getInputStream()), responseClass);
    }

    private record ErrorResponse(String message) {}

    public record RegisterRequest(String username, String password, String email) {}
    public record LoginRequest(String username, String password) {}
    public record CreateGameRequest(String gameName) {}
    public record JoinGameRequest(String playerColor, int gameID) {}
    public record ListGamesResponse(java.util.List<model.GameData> games) {}
}
