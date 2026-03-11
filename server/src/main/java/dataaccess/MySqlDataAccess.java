package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = """
               SELECT username, password, email
               FROM user
               WHERE username = ?
               """;
        try (var con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("unable to get user", e);
        }
    }
}
