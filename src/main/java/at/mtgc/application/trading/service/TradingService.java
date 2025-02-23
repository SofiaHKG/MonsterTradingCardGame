package at.mtgc.application.trading.service;

import at.mtgc.application.trading.entity.TradingDeal;
import at.mtgc.application.trading.repository.TradingRepository;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.application.packages.entity.Card;
import at.mtgc.server.http.Status;
import at.mtgc.server.http.HttpException;
import at.mtgc.server.util.DatabaseManager;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class TradingService {

    private final TradingRepository tradingRepository;
    private final UserRepository userRepository;

    public TradingService(TradingRepository tradingRepository, UserRepository userRepository) {
        this.tradingRepository = tradingRepository;
        this.userRepository = userRepository;
    }

    public List<TradingDeal> getAllDeals() {
        return tradingRepository.findAllDeals();
    }

    public void createDeal(String username, TradingDeal deal) throws SQLException {
        // 1) Check if card belongs to user
        Card cardToTrade = findCardById(deal.getCardToTrade());
        if(cardToTrade == null) {
            throw new HttpException(Status.NOT_FOUND, "Card not found in DB");
        }

        UUID cardUUID = UUID.fromString(cardToTrade.getId());

        String owner = getOwnerOfCard(cardUUID);
        if(owner == null) {
            throw new HttpException(Status.NOT_FOUND, "No owner found for card, DB inconsistency?");
        }
        if(!username.equals(owner)) {
            throw new HttpException(Status.FORBIDDEN, "You do not own this card");
        }

        if(isCardInDeck(cardUUID)) {
            throw new HttpException(Status.FORBIDDEN, "Card is locked in deck");
        }

        if(isCardLockedForTrade(cardUUID)) {
            throw new HttpException(Status.FORBIDDEN, "Card is already locked for trading");
        }

        lockCardForTrade(cardUUID, true);

        deal.setOwner(username);

        if(tradingRepository.findDealById(deal.getId()) != null) {
            // rollback
            lockCardForTrade(cardUUID, false);
            throw new HttpException(Status.CONFLICT, "Deal with this ID already exists");
        }

        // Insert into DB
        tradingRepository.createDeal(deal, username);
    }

    public void deleteDeal(String username, String dealId) throws SQLException {
        UUID dealUUID = UUID.fromString(dealId);
        TradingDeal deal = tradingRepository.findDealById(dealUUID);
        if(deal == null) {
            throw new HttpException(Status.NOT_FOUND, "Deal not found");
        }
        // only owner can delete
        if(!deal.getOwner().equals(username)) {
            throw new HttpException(Status.FORBIDDEN, "You are not the owner of this deal");
        }

        // Unlock the card
        UUID cardUUID = deal.getCardToTrade(); // in TradingDeal ist das schon ein UUID
        lockCardForTrade(cardUUID, false);

        // Delete the deal
        tradingRepository.deleteDeal(dealUUID);
    }

    public void executeTrade(String buyerName, String dealId, String offeredCardId) throws SQLException {
        UUID dealUUID = UUID.fromString(dealId);
        TradingDeal deal = tradingRepository.findDealById(dealUUID);
        if(deal == null) {
            throw new HttpException(Status.NOT_FOUND, "Deal not found");
        }

        // Check not same user
        if(deal.getOwner().equals(buyerName)) {
            throw new HttpException(Status.FORBIDDEN, "You cannot trade with yourself");
        }

        //Find offeredCard
        UUID offeredUUID = UUID.fromString(offeredCardId);
        Card offeredCard = findCardById(offeredUUID);
        if(offeredCard == null) {
            throw new HttpException(Status.NOT_FOUND, "Offered card not found");
        }

        // Check if offeredCard belongs to buyer
        UUID offeredCardUUID = UUID.fromString(offeredCard.getId());
        String offeredOwner = getOwnerOfCard(offeredCardUUID);
        if(!buyerName.equals(offeredOwner)) {
            throw new HttpException(Status.FORBIDDEN, "You do not own the offered card");
        }

        // Check if offeredCard is in deck
        if(isCardInDeck(offeredCardUUID)) {
            throw new HttpException(Status.FORBIDDEN, "Offered card is in deck");
        }

        // Check damage + type
        if(!getCardType(offeredCard.getName()).equalsIgnoreCase(deal.getType())) {
            throw new HttpException(Status.FORBIDDEN, "Card type does not match the required type");
        }
        if(offeredCard.getDamage() < deal.getMinimumDamage()) {
            throw new HttpException(Status.FORBIDDEN, "Offered card damage is too low");
        }

        // If trade valid, do the swap inside a DB transaction
        doTradeTransaction(deal, buyerName, offeredCard);
    }

    private void doTradeTransaction(TradingDeal deal, String buyerName, Card offeredCard) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            updateCardOwner(conn, deal.getCardToTrade(), buyerName);
            UUID offeredCardUUID = UUID.fromString(offeredCard.getId());
            updateCardOwner(conn, offeredCardUUID, deal.getOwner());
            updateLockedForTrade(conn, deal.getCardToTrade(), false);

            deleteDealInTx(conn, deal.getId());

            conn.commit();
        } catch(SQLException e) {
            if(conn != null) { conn.rollback(); }
            throw e;
        } finally {
            if(conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private void deleteDealInTx(Connection conn, UUID dealId) throws SQLException {
        String sql = "DELETE FROM trading_deals WHERE id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, dealId);
            stmt.executeUpdate();
        }
    }

    private void updateCardOwner(Connection conn, UUID cardId, String newOwner) throws SQLException {
        String sql = "UPDATE cards SET owner = ? WHERE id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newOwner);
            stmt.setObject(2, cardId);
            stmt.executeUpdate();
        }
    }

    private void updateLockedForTrade(Connection conn, UUID cardId, boolean locked) throws SQLException {
        String sql = "UPDATE cards SET locked_for_trade = ? WHERE id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, locked);
            stmt.setObject(2, cardId);
            stmt.executeUpdate();
        }
    }

    private Card findCardById(UUID cardId) {
        String sql = "SELECT id, name, damage FROM cards WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                // Achtung: in DB ist 'id' = UUID, wir bauen uns aber
                // ein Card-Objekt mit String-Id:
                return new Card(
                        rs.getString("id"),     // e.g. "845f0dc7-..."
                        rs.getString("name"),
                        rs.getDouble("damage")
                );
            }
        } catch(SQLException e) {
            System.err.println("Error findCardById: " + e.getMessage());
        }
        return null;
    }

    private String getOwnerOfCard(UUID cardId) {
        String sql = "SELECT owner FROM cards WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getString("owner");
            }
        } catch(SQLException e) {
            System.err.println("Error getOwnerOfCard: " + e.getMessage());
        }
        return null;
    }

    private boolean isCardInDeck(UUID cardId) {
        String sql = "SELECT in_deck FROM cards WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getBoolean("in_deck");
            }
        } catch(SQLException e) {
            System.err.println("Error isCardInDeck: " + e.getMessage());
        }
        return false;
    }

    private boolean isCardLockedForTrade(UUID cardId) {
        String sql = "SELECT locked_for_trade FROM cards WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getBoolean("locked_for_trade");
            }
        } catch(SQLException e) {
            System.err.println("Error isCardLockedForTrade: " + e.getMessage());
        }
        return false;
    }

    private void lockCardForTrade(UUID cardId, boolean lock) {
        String sql = "UPDATE cards SET locked_for_trade = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, lock);
            stmt.setObject(2, cardId);
            stmt.executeUpdate();
        } catch(SQLException e) {
            System.err.println("Error lockCardForTrade: " + e.getMessage());
        }
    }

    private String getCardType(String cardName) {
        // if cardName ends with "Spell" => "spell"
        // sonst => "monster"
        String lower = cardName.toLowerCase();
        if(lower.contains("spell")) {
            return "spell";
        }
        return "monster";
    }
}
