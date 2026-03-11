package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySqlDataAccess {
    public mySqlDataAccess() {}

    @Override
    public void clear () throws DataAccessException {
        String deleteAuth = "DELETE FROM auth";
        String deleteGame = "DELETE FROM game";
        String deleteUser = "DELETE FROM user";
        try (var con = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(deleteAuth)) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(deleteGame)) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(deleteUser)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("unable to clear database", e);
        }
    }
}
