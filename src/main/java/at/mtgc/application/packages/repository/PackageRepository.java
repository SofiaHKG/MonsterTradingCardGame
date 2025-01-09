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
        String sql = "INSERT INTO cards (id, name, damage, package_id) VALUES (?,?,?,?)";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, UUID.fromString(card.getId()));
            stmt.setString(2, card.getName());
            stmt.setDouble(3, card.getDamage());
            stmt.setInt(4, packageId);
            stmt.executeUpdate();
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
                return getPackageById(packageId);
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
}
