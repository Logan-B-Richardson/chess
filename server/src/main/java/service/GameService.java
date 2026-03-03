package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
    private final DataAccess dao;

    public GameService(DataAccess dao) {
        this.dao = dao;
    }

    public ListGameResults listGames(String token) {
        if (token == null || dao.getAuth(token) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        List<GameSummary> games = dao.listGames().stream().map(g -> new GameSummary(g.gameid(), g.whiteusername(), g.blackusername(), g.gamename())).toList();
        return new ListGameResults(games);
    }

    public CreateGameResult createGame(String token, CreateGameRequest register) {
        if (token == null || dao.getAuth(token) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        if (register == null || register.gamename() == null) {
            throw new BadRequestException("bad request");
        }
        int id = dao.createGame(register.gamename());
        return new CreateGameResult(id);
    }
}
