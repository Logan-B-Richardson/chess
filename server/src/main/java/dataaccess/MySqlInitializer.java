package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySqlInitializer {
    public static void configureDatabase() {
        try {
            DatabaseManager.createDatabase();
            try (Connection con = DatabaseManager.getConnection()) {
                createUserTable(con);
                createAuthTable(con);
                createGameTable(con);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize database", e);
        }
    }

    private static void createUserTable(Connection con) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS user (
                username VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                PRIMARY KEY (username)
                )
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private static void createAuthTable(Connection con) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL,
                PRIMARY KEY (authToken)
                )
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private static void createGameTable(Connection con) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS game (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                gameState LONGTEXT,
                PRIMARY KEY (gameID)
                )
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
