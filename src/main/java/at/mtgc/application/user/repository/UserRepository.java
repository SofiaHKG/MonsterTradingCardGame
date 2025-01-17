package at.mtgc.application.user.repository;

import at.mtgc.application.user.entity.User;
import at.mtgc.application.packages.entity.Card;
import at.mtgc.server.util.DatabaseManager;

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
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("token"),
                        rs.getInt("coins")
                );
            } else {
                System.out.println("User not found in database");  // Debug
            }
        } catch(SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    public List<Card> getUserCards(String username) {
        System.out.println("Executing SQL query: SELECT id, name, damage FROM cards WHERE owner = '" + username + "'"); // Debug

        String sql = "SELECT id, name, damage FROM cards WHERE owner = ?";
        List<Card> cards = new ArrayList<>();

        try(Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            System.out.println("Executing SQL: " + stmt.toString()); // Debugging
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                cards.add(new Card(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("damage")
                ));
            }

            if(cards.isEmpty()) {
                System.out.println("No cards found for user: " + username); // Debugging
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
            System.out.println("Executing SQL: " + stmt.toString()); // Debugging
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                deck.add(new Card(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("damage")
                ));
            }

            if(deck.size() != 4) {
                System.out.println("User " + username + " has an incomplete deck."); // Debugging
                return new ArrayList<>(); // Returns empty list
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

            // Remove all of the user's cards from the deck
            System.out.println("Executing SQL: " + resetDeckSQL + " with username = " + username); // Debugging
            resetStmt.setString(1, username);
            resetStmt.executeUpdate();

            // Put the new 4 cards into the deck
            for(String cardId : cardIds) {
                System.out.println("Executing SQL: " + updateDeckSQL + " with username = " + username + " and card ID = " + cardId); // Debugging
                updateStmt.setString(1, username);
                updateStmt.setObject(2, UUID.fromString(cardId));
                int rowsAffected = updateStmt.executeUpdate();

                if(rowsAffected == 0) {
                    System.err.println("Failed to update deck: Card ID " + cardId + " not found for user " + username);
                    return false;
                }
            }

            System.out.println("Deck successfully updated for user: " + username); // Debugging
            return true;

        } catch(SQLException e) {
            System.err.println("Error updating deck for user " + username + ": " + e.getMessage());
            return false;
        }
    }

}