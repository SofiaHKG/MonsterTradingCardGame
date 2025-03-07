package at.mtgc.application.user.repository;

import at.mtgc.application.user.entity.User;
import at.mtgc.application.packages.entity.Card;
import at.mtgc.server.util.DatabaseManager;
import at.mtgc.application.user.entity.ScoreboardEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    // Find by username (for login)
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("token"),
                        rs.getInt("coins"),
                        rs.getString("fullname"),
                        rs.getString("bio"),
                        rs.getString("image")
                );
                user.setWins(rs.getInt("wins"));
                user.setLosses(rs.getInt("losses"));
                user.setElo(rs.getInt("elo"));
                return user;
            }
        } catch(SQLException e) {
            System.err.println("Error retrieving user (findByUsername): " + e.getMessage());
        }
        return null;
    }

    public boolean updateToken(String username, String token) {
        String sql = "UPDATE users SET token = ? WHERE username = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch(SQLException e) {
            System.err.println("Error updating token: " + e.getMessage());
            return false;
        }
    }

    public User getUserByUsername(String username) {
        System.out.println("Fetching user: " + username);  // Debug
        String sql = "SELECT * FROM users WHERE username = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                System.out.println("User found: " + rs.getString("username"));  // Debug
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("token"),
                        rs.getInt("coins"),
                        rs.getString("fullname"),
                        rs.getString("bio"),
                        rs.getString("image")
                );
                user.setWins(rs.getInt("wins"));
                user.setLosses(rs.getInt("losses"));
                user.setElo(rs.getInt("elo"));
                return user;
            } else {
                System.out.println("User not found in database");  // Debug
            }
        } catch(SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    public List<Card> getUserCards(String username) {
        String sql = "SELECT id, name, damage FROM cards WHERE owner = ?";
        List<Card> cards = new ArrayList<>();

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                cards.add(new Card(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("damage")
                ));
            }
        } catch(SQLException e) {
            System.err.println("Error retrieving cards for user " + username + ": " + e.getMessage());
        }
        return cards;
    }

    public List<Card> getUserDeck(String username) {
        String sql = "SELECT id, name, damage FROM cards WHERE owner = ? AND in_deck = TRUE";
        List<Card> deck = new ArrayList<>();

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                deck.add(new Card(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("damage")
                ));
            }

            // If not exactly 4 cards then return empty list
            if(deck.size() != 4) {
                System.out.println("User " + username + " deck not 4 => empty deck returned");
                return new ArrayList<>();
            }
        } catch(SQLException e) {
            System.err.println("Error retrieving deck for user " + username + ": " + e.getMessage());
        }
        return deck;
    }

    public boolean updateUserDeck(String username, List<String> cardIds) {
        String resetDeckSQL = "UPDATE cards SET in_deck = FALSE WHERE owner = ?";
        String updateDeckSQL = "UPDATE cards SET in_deck = TRUE WHERE owner = ? AND id = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement resetStmt = conn.prepareStatement(resetDeckSQL);
            PreparedStatement updateStmt = conn.prepareStatement(updateDeckSQL)) {

            // Remove all deck cards
            resetStmt.setString(1, username);
            resetStmt.executeUpdate();

            // Activate the four new deck cards
            for(String cardId : cardIds) {
                updateStmt.setString(1, username);
                updateStmt.setObject(2, UUID.fromString(cardId));
                int rowsAffected = updateStmt.executeUpdate();
                if(rowsAffected == 0) {
                    System.err.println("Failed to put card " + cardId + " into deck of " + username);
                    return false;
                }
            }
            return true;
        } catch(SQLException e) {
            System.err.println("Error updating deck for " + username + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(String username, User updatedUser) {
        User existingUser = getUserByUsername(username);
        if(existingUser == null) {
            return false;
        }

        String sql = "UPDATE users SET fullname = ?, bio = ?, image = ?, password = COALESCE(?, password) WHERE username = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, updatedUser.getFullname());
            stmt.setString(2, updatedUser.getBio());
            stmt.setString(3, updatedUser.getImage());
            stmt.setString(4, updatedUser.getPassword());
            stmt.setString(5, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch(SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public User getUserStats(String username) {
        String sql = "SELECT username, wins, losses, elo FROM users WHERE username = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getInt("wins"),
                        rs.getInt("losses"),
                        rs.getInt("elo")
                );
                return user;
            }
        } catch(SQLException e) {
            System.err.println("Error retrieving stats: " + e.getMessage());
        }
        return null;
    }

    public List<ScoreboardEntry> getScoreboard() {
        String sql = "SELECT username, wins, losses, elo FROM users ORDER BY elo DESC";
        List<ScoreboardEntry> scoreboard = new ArrayList<>();

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while(rs.next()) {
                scoreboard.add(new ScoreboardEntry(
                        rs.getString("username"),
                        rs.getInt("wins"),
                        rs.getInt("losses"),
                        rs.getInt("elo")
                ));
            }
        } catch(SQLException e) {
            System.err.println("Error retrieving scoreboard: " + e.getMessage());
        }
        return scoreboard;
    }

    // ELO and stats
    public boolean updateUserStats(User user) {
        String sql = "UPDATE users SET wins = ?, losses = ?, elo = ?, coins = ? WHERE username = ?";
        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getWins());
            stmt.setInt(2, user.getLosses());
            stmt.setInt(3, user.getElo());
            stmt.setInt(4, user.getCoins());
            stmt.setString(5, user.getUsername());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch(SQLException e) {
            System.err.println("Error updating user stats: " + e.getMessage());
        }
        return false;
    }
}