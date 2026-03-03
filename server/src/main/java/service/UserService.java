package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final DataAccess dao;

    public UserService(DataAccess dao) {
        this.dao = dao;
    }

    public RegisterResult register(RegisterRequest register) {
        if (register == null ||
                register.username() == null ||
                register.password() == null ||
                register.email() == null) {
            throw new BadRequestException("bad request");
        }
        if (dao.getUser(register.username()) != null) {
            throw new AlreadyTakenException("username taken");
        }
        dao.createUser(new UserData(register.username(), register.password(), register.email()));
        String token = UUID.randomUUID().toString();
        return new RegisterResult(register.username(), token);
    }

    public LoginResult login(LoginRequest register) {
        if (register == null ||
                register.username() == null ||
                register.password() == null) {
            throw new BadRequestException("bad request");
        }
        UserData user = dao.getUser(register.username());
        if (user == null || !user.password().equals(register.password())) {
            throw new UnauthorizedException("wrong password");
        }
        String token = UUID.randomUUID().toString();
        dao.createAuth(new AuthData(token, register.username()));
        return new LoginResult(register.username(), token);
    }
}
