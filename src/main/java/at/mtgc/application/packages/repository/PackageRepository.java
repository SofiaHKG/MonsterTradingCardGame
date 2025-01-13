package at.mtgc.application.packages.repository;

import at.mtgc.application.packages.entity.Card;
import at.mtgc.application.packages.entity.Package;
import at.mtgc.server.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PackageRepository {

    public void addPackage(Package pack) {
        String sql = "INSERT INTO packages DEFAULT VALUES RETURNING id";

        try(Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();

            if(rs.next()) {
                int packageId = rs.getInt(1);

                for(Card card : pack.getCards()) {
                    addCardToDatabase(card, packageId);
                }
            }
        } catch(SQLException e) {
            System.err.println("Error saving package: " + e.getMessage());
        }
    }

    private void addCardToDatabase(Card card, int packageId) {
        String checkSql = "SELECT COUNT(*) FROM cards WHERE id = ?";
        String insertSql = "INSERT INTO cards (id, name, damage, package_id) VALUES (?,?,?,?)";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setObject(1, UUID.fromString(card.getId()));
            ResultSet rs = checkStmt.executeQuery();
            if(rs.next() && rs.getInt(1) > 0) {
                System.err.println("Card with ID " + card.getId() + " already exists. Skipping insertion.");
                return;
            }

            insertStmt.setObject(1, UUID.fromString(card.getId()));
            insertStmt.setString(2, card.getName());
            insertStmt.setDouble(3, card.getDamage());
            insertStmt.setInt(4, packageId);
            insertStmt.executeUpdate();

        } catch(SQLException e) {
            System.err.println("Error saving card: " + e.getMessage());
        }
    }

    public Package getNextPackage() {
        String sql = "SELECT id FROM packages ORDER BY id ASC LIMIT 1";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            if(rs.next()) {
                int packageId = rs.getInt("id");
                System.out.println("Found package with ID: " + packageId); // Debug
                return getPackageById(packageId);
            } else {
                System.out.println("No packages available"); // Debug
            }
        } catch(SQLException e) {
            System.err.println("Error getting next package: " + e.getMessage());
        }
        return null;
    }

    private Package getPackageById(int packageId) {
        String sql = "SELECT * FROM cards WHERE package_id = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, packageId);
            ResultSet rs = stmt.executeQuery();

            List<Card> cards = new ArrayList<>();
            while(rs.next()) {
                Card card = new Card(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("damage")
                );
                cards.add(card);
            }

            return new Package(cards);
        } catch(SQLException e) {
            System.err.println("Error getting package: " + e.getMessage());
        }
        return null;
    }

    public boolean hasPackages() {
        String sql = "SELECT COUNT(*) FROM packages";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            if(rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch(SQLException e) {
            System.err.println("Error checking for packages: " + e.getMessage());
        }
        return false;
    }

    public boolean acquirePackage(String username) {
        String checkCoinsSQL = "SELECT coins FROM users WHERE username = ?";
        String updateCoinsSQL = "UPDATE users SET coins = coins - 5 WHERE username = ?";
        String getPackageSQL = "SELECT id FROM packages ORDER BY id ASC LIMIT 1";
        String assignCardsSQL = "UPDATE cards SET package_id = NULL, owner = ? WHERE package_id = ?";

        try(Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkCoinsStmt = conn.prepareStatement(checkCoinsSQL);
             PreparedStatement updateCoinsStmt = conn.prepareStatement(updateCoinsSQL);
             PreparedStatement getPackageStmt = conn.prepareStatement(getPackageSQL);
             PreparedStatement assignCardsStmt = conn.prepareStatement(assignCardsSQL)) {

            checkCoinsStmt.setString(1, username);
            ResultSet rs = checkCoinsStmt.executeQuery();

            if(!rs.next() || rs.getInt("coins") < 5) {
                return false;
            }

            ResultSet packageRs = getPackageStmt.executeQuery();
            if(!packageRs.next()) {
                return false;
            }

            int packageId = packageRs.getInt("id");

            updateCoinsStmt.setString(1, username);
            updateCoinsStmt.executeUpdate();

            assignCardsStmt.setString(1, username);
            assignCardsStmt.setInt(2, packageId);
            assignCardsStmt.executeUpdate();

            String deletePackageSQL = "DELETE FROM packages WHERE id = ?";
            try(PreparedStatement deletePackageStmt = conn.prepareStatement(deletePackageSQL)) {
                deletePackageStmt.setInt(1, packageId);
                deletePackageStmt.executeUpdate();
            }

            return true;
        } catch(SQLException e) {
            System.err.println("Error acquiring package: " + e.getMessage());
        }
        return false;
    }
}
