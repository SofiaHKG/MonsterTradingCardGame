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

        try(Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try(PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();

                if(rs.next()) {
                    int packageId = rs.getInt(1);

                    for(Card card : pack.getCards()) {
                        if(!addCardToDatabase(card, packageId, conn)) {
                            conn.rollback();
                            System.err.println("Rolling back package creation due to existing card: " + card.getId());
                            return;
                        }
                    }
                    conn.commit();
                }
            }
        } catch(SQLException e) {
            System.err.println("Error saving package: " + e.getMessage());
        }
    }

    private boolean addCardToDatabase(Card card, int packageId, Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM cards WHERE id = ?";
        String insertSql = "INSERT INTO cards (id, name, damage, package_id) VALUES (?,?,?,?)";

        try(PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setObject(1, UUID.fromString(card.getId()));
            ResultSet rs = checkStmt.executeQuery();
            if(rs.next() && rs.getInt(1) > 0) {
                System.err.println("Card with ID " + card.getId() + " already exists. Skipping insertion.");
                return false;
            }

            insertStmt.setObject(1, UUID.fromString(card.getId()));
            insertStmt.setString(2, card.getName());
            insertStmt.setDouble(3, card.getDamage());
            insertStmt.setInt(4, packageId);
            insertStmt.executeUpdate();
            return true;

        } catch(SQLException e) {
            System.err.println("Error saving card: " + e.getMessage());
        }
        return false;
    }

    public Package getNextPackage() {
        String sql = "SELECT id FROM packages ORDER BY id ASC LIMIT 1";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            if(rs.next()) {
                int packageId = rs.getInt("id");
                System.out.println("Found package with ID: " + packageId); // Debug
                Package pkg = getPackageById(packageId);
                return (pkg != null) ? pkg : null;
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
        String assignCardsSQL = "UPDATE cards SET owner = ? WHERE package_id = ?";
        String resetPackageIdSQL = "UPDATE cards SET package_id = NULL WHERE package_id = ?";
        String deletePackageSQL = "DELETE FROM packages WHERE id = ?";

        try(Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try(PreparedStatement checkCoinsStmt = conn.prepareStatement(checkCoinsSQL);
                PreparedStatement updateCoinsStmt = conn.prepareStatement(updateCoinsSQL);
                PreparedStatement getPackageStmt = conn.prepareStatement(getPackageSQL);
                PreparedStatement assignCardsStmt = conn.prepareStatement(assignCardsSQL);
                PreparedStatement resetPackageIdStmt = conn.prepareStatement(resetPackageIdSQL);
                PreparedStatement deletePackageStmt = conn.prepareStatement(deletePackageSQL)) {

                // Check if enough coins available
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

                // Withdraw coins
                updateCoinsStmt.setString(1, username);
                updateCoinsStmt.executeUpdate();

                // Assign cards
                assignCardsStmt.setString(1, username);
                assignCardsStmt.setInt(2, packageId);
                int updatedRows = assignCardsStmt.executeUpdate();
                if(updatedRows == 0) {
                    conn.rollback();
                    System.err.println("Couldn't assign cards to package " + packageId);
                    return false;
                }

                // Reset package ID and delete package
                resetPackageIdStmt.setInt(1, packageId);
                resetPackageIdStmt.executeUpdate();
                deletePackageStmt.setInt(1, packageId);
                deletePackageStmt.executeUpdate();

                conn.commit();
                return true;

            } catch(SQLException e) {
                conn.rollback();
                System.err.println("Error acquiring package: " + e.getMessage());
            }
        } catch(SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        return false;
    }
}
