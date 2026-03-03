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
        List<GameSummary> games = dao.listGames().stream().map(g -> new GameSummary(
                        g.gameid(),
                        g.gamename(),
                        g.whiteusername(),
                        g.blackusername()
                ))
                .toList();
        return new ListGameResults(games);
    }

    public CreateGameResult createGame(String token, CreateGameRequest request) {
        if (request == null || request.gamename() == null) {
            throw new BadRequestException("bad request");
        }
        if (token == null || dao.getAuth(token) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        int id = dao.createGame(request.gamename());
        return new CreateGameResult(id);
    }

    public void joinGame(String token, JoinGameRequest request) {
        if (request == null ||
                request.gameid() == 0 ||
                request.playercolor() == null) {
            throw new BadRequestException("bad request");
        }
        GameData game = dao.getGame(request.gameid());
        if (game == null) {
            throw new BadRequestException("bad request");
        }
        if (token == null || dao.getAuth(token) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        String color = request.playercolor();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new BadRequestException("bad request");
        }
        AuthData auth = dao.getAuth(token);
        if (color.equals("WHITE")) {
            if (game.whiteusername() != null) {
                throw new ForbiddenException("already taken");
            }
            game = new GameData(game.gameid(), game.game(), auth.username(), game.blackusername(), game.gamename());
        } else {
            if (game.blackusername() != null) {
                throw new ForbiddenException("already taken");
            }
            game = new GameData(game.gameid(), game.game(), game.whiteusername(), auth.username(), game.gamename());
        }
        dao.updateGame(game);
    }
}
