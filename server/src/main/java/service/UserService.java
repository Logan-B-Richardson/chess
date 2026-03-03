package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import service.exceptions.AlreadyTakenException;
import service.exceptions.BadRequestException;
import service.exceptions.UnauthorizedException;
import service.records.LoginRequest;
import service.records.LoginResult;
import service.records.RegisterRequest;
import service.records.RegisterResult;

import java.util.UUID;

public class UserService {
    private final DataAccess dao;

    public UserService(DataAccess dao) {
        this.dao = dao;
    }

    public RegisterResult register(RegisterRequest request) {
        if (request == null ||
                request.username() == null ||
                request.password() == null ||
                request.email() == null) {
            throw new BadRequestException("bad request");
        }
        if (dao.getUser(request.username()) != null) {
            throw new AlreadyTakenException("username taken");
        }
        dao.createUser(new UserData(request.username(), request.password(), request.email()));
        String token = UUID.randomUUID().toString();
        dao.createAuth(new AuthData(token, request.username()));
        return new RegisterResult(request.username(), token);
    }

    public LoginResult login(LoginRequest request) {
        if (request == null ||
                request.username() == null ||
                request.password() == null) {
            throw new BadRequestException("bad request");
        }
        UserData user = dao.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new UnauthorizedException("wrong password");
        }
        String token = UUID.randomUUID().toString();
        dao.createAuth(new AuthData(token, request.username()));
        return new LoginResult(request.username(), token);
    }

    public void logout(String token) {
        if (token == null || dao.getAuth(token) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        dao.deleteAuth(token);
    }
}
