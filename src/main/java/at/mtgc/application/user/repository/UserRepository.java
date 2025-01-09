package at.mtgc.application.user.repository;

import at.mtgc.application.user.entity.User;
import at.mtgc.server.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    public boolean save(User user) {

        String sql = "INSERT INTO users (username, password, token) VALUES (?, ?, ?)";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getToken());
            stmt.executeUpdate();
            return true;
        } catch(SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
            return false;
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                User user = new User(rs.getString("username"), rs.getString("password"));
                user.setToken(rs.getString("token"));
                return user;
            }
        } catch(SQLException e) {
            System.err.println("Error retrieving user: " + e.getMessage());
        }
        return null;
    }

    public boolean updateToken(String username, String token) {
        String sql = "UPDATE users SET token = ? WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating token: " + e.getMessage());
            return false;
        }
    }
}