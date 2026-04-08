package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import service.exceptions.BadRequestException;
import service.exceptions.ForbiddenException;
import service.exceptions.UnauthorizedException;
import service.records.*;

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
        if (request == null || request.gameName() == null) {
            throw new BadRequestException("bad request");
        }
        if (token == null || dao.getAuth(token) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        int id = dao.createGame(request.gameName());
        return new CreateGameResult(id);
    }

    public void joinGame(String token, JoinGameRequest request) {
        if (request == null ||
                request.gameID() == 0 ||
                request.playerColor() == null) {
            throw new BadRequestException("bad request");
        }
        GameData game = dao.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("bad request");
        }
        if (token == null || dao.getAuth(token) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        String color = request.playerColor();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new BadRequestException("bad request");
        }
        AuthData auth = dao.getAuth(token);
        if (color.equals("WHITE")) {
            if (game.whiteusername() != null) {
                throw new ForbiddenException("already taken");
            }
            game = new GameData(game.gameid(), auth.username(), game.blackusername(), game.gamename(), game.game());
        } else {
            if (game.blackusername() != null) {
                throw new ForbiddenException("already taken");
            }
            game = new GameData(game.gameid(), game.whiteusername(), auth.username(), game.gamename(), game.game());
        }
        dao.updateGame(game);
    }
}
