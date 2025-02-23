package at.mtgc.application.trading.repository;

import at.mtgc.application.trading.entity.TradingDeal;
import at.mtgc.server.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradingRepository {

    // Create deal
    public void createDeal(TradingDeal deal, String owner) throws SQLException {
        String sql = """
            INSERT INTO trading_deals (id, card_to_trade, owner, required_type, min_damage)
            VALUES (?, ?, ?, ?, ?)
        """;

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, deal.getId());            // UUID
            stmt.setObject(2, deal.getCardToTrade());   // UUID
            stmt.setString(3, owner);
            stmt.setString(4, deal.getType());
            stmt.setDouble(5, deal.getMinimumDamage());

            stmt.executeUpdate();
        }
    }

    public TradingDeal findDealById(UUID dealId) {
        String sql = "SELECT * FROM trading_deals WHERE id = ?";
        try(Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, dealId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                TradingDeal deal = new TradingDeal();
                deal.setId((UUID) rs.getObject("id"));
                deal.setCardToTrade((UUID) rs.getObject("card_to_trade"));
                deal.setType(rs.getString("required_type"));
                deal.setMinimumDamage(rs.getDouble("min_damage"));
                deal.setOwner(rs.getString("owner"));
                return deal;
            }
        } catch(SQLException e) {
            System.err.println("Error finding deal by id: " + e.getMessage());
        }
        return null;
    }

    public List<TradingDeal> findAllDeals() {
        List<TradingDeal> deals = new ArrayList<>();
        String sql = "SELECT * FROM trading_deals";
        try(Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while(rs.next()) {
                TradingDeal deal = new TradingDeal();
                deal.setId((UUID) rs.getObject("id"));
                deal.setCardToTrade((UUID) rs.getObject("card_to_trade"));
                deal.setType(rs.getString("required_type"));
                deal.setMinimumDamage(rs.getDouble("min_damage"));
                deal.setOwner(rs.getString("owner"));
                deals.add(deal);
            }
        } catch(SQLException e) {
            System.err.println("Error listing trading deals: " + e.getMessage());
        }
        return deals;
    }

    public void deleteDeal(UUID dealId) throws SQLException {
        String sql = "DELETE FROM trading_deals WHERE id = ?";
        try(Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, dealId);
            stmt.executeUpdate();
        }
    }

}