package integration.at.mtgc.application.user.controller;

import at.mtgc.server.http.Method;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.server.util.DatabaseManager;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.application.user.service.UserService;
import at.mtgc.application.user.controller.UserController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeckConfigurationIntegrationTests {

    private UserController userController;

    @BeforeEach
    public void setUp() {
        cleanupDatabase();

        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        userController = new UserController(userService);
    }

    @AfterEach
    public void tearDown() {
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        try(Connection conn = DatabaseManager.getConnection()) {
            conn.prepareStatement("DELETE FROM trading_deals").executeUpdate();
            conn.prepareStatement("DELETE FROM cards").executeUpdate();
            conn.prepareStatement("DELETE FROM packages").executeUpdate();
            conn.prepareStatement("DELETE FROM users WHERE username <> 'admin'").executeUpdate();
        } catch(SQLException e) {
            throw new RuntimeException("DB cleanup failed", e);
        }
    }

    // Helper method to insert a card for a user into the DB
    private void addCardForUser(String username, String cardId, String name, double damage) {
        try(Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO cards (id, name, damage, owner, in_deck) VALUES (?, ?, ?, ?, false)"
             )) {
            stmt.setObject(1, UUID.fromString(cardId));
            stmt.setString(2, name);
            stmt.setDouble(3, damage);
            stmt.setString(4, username);
            stmt.executeUpdate();
        } catch(SQLException e) {
            fail("Failed to add card for user " + username + ": " + e.getMessage());
        }
    }

    @Test
    public void testConfigureDeckSuccess() {
        String username = "deckUser";
        // Registering user
        Request regReq = new Request();
        regReq.setMethod(Method.POST);
        regReq.setPath("/users");
        regReq.setBody("{\"Username\":\"" + username + "\", \"Password\":\"pass\"}");
        Response regResp = userController.handle(regReq);
        assertEquals(Status.CREATED, regResp.getStatus(), "User registration should succeed");

        // Insert 4 cards for the user
        String cardId1 = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        String cardId2 = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
        String cardId3 = "cccccccc-cccc-cccc-cccc-cccccccccccc";
        String cardId4 = "dddddddd-dddd-dddd-dddd-dddddddddddd";
        addCardForUser(username, cardId1, "WaterGoblin", 10.0);
        addCardForUser(username, cardId2, "Dragon", 50.0);
        addCardForUser(username, cardId3, "WaterSpell", 20.0);
        addCardForUser(username, cardId4, "Ork", 45.0);

        // Send PUT /deck request to configure the deck with exactly 4 cards
        Request updateDeckReq = new Request();
        updateDeckReq.setMethod(Method.PUT);
        updateDeckReq.setPath("/deck");
        updateDeckReq.setBody("[\"" + cardId1 + "\", \"" + cardId2 + "\", \"" + cardId3 + "\", \"" + cardId4 + "\"]");
        updateDeckReq.setHeader("Authorization", "Bearer " + username + "-mtcgToken");
        Response updateDeckResp = userController.handle(updateDeckReq);
        assertEquals(Status.OK, updateDeckResp.getStatus(), "Deck configuration should return 200 OK");
        assertTrue(updateDeckResp.getBody().contains("Deck updated successfully"));
    }

    @Test
    public void testConfigureDeckTooFewCards() {
        String username = "deckUser2";
        // Registering user
        Request regReq = new Request();
        regReq.setMethod(Method.POST);
        regReq.setPath("/users");
        regReq.setBody("{\"Username\":\"" + username + "\", \"Password\":\"pass\"}");
        Response regResp = userController.handle(regReq);
        assertEquals(Status.CREATED, regResp.getStatus(), "User registration should succeed");

        // Insert 3 cards for the user
        String cardId1 = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee";
        String cardId2 = "ffffffff-ffff-ffff-ffff-ffffffffffff";
        String cardId3 = "11111111-2222-3333-4444-555555555555";
        addCardForUser(username, cardId1, "WaterGoblin", 10.0);
        addCardForUser(username, cardId2, "Dragon", 50.0);
        addCardForUser(username, cardId3, "WaterSpell", 20.0);

        // Send PUT /deck request with only 3 cards
        Request updateDeckReq = new Request();
        updateDeckReq.setMethod(Method.PUT);
        updateDeckReq.setPath("/deck");
        updateDeckReq.setBody("[\"" + cardId1 + "\", \"" + cardId2 + "\", \"" + cardId3 + "\"]");
        updateDeckReq.setHeader("Authorization", "Bearer " + username + "-mtcgToken");
        Response updateDeckResp = userController.handle(updateDeckReq);
        assertEquals(Status.BAD_REQUEST, updateDeckResp.getStatus(), "Configuring a deck with fewer than 4 cards should return 400 BAD REQUEST");
        assertTrue(updateDeckResp.getBody().contains("Deck must contain exactly 4 cards"));
    }

    @Test
    public void testConfigureDeckWithInvalidCards() {
        String username = "deckUser3";
        // Registering user
        Request regReq = new Request();
        regReq.setMethod(Method.POST);
        regReq.setPath("/users");
        regReq.setBody("{\"Username\":\"" + username + "\", \"Password\":\"pass\"}");
        Response regResp = userController.handle(regReq);
        assertEquals(Status.CREATED, regResp.getStatus(), "User registration should succeed");

        // Insert 3 valid cards for the user
        String cardId1 = "66666666-6666-6666-6666-666666666667";
        String cardId2 = "77777777-7777-7777-7777-777777777778";
        String cardId3 = "88888888-8888-8888-8888-888888888889";
        addCardForUser(username, cardId1, "WaterGoblin", 10.0);
        addCardForUser(username, cardId2, "Dragon", 50.0);
        addCardForUser(username, cardId3, "WaterSpell", 20.0);

        // Use one card ID that does NOT belong to the user
        String invalidCardId = "99999999-9999-9999-9999-999999999998";

        // Send PUT /deck request with 4 card IDs (one of which is invalid)
        Request updateDeckReq = new Request();
        updateDeckReq.setMethod(Method.PUT);
        updateDeckReq.setPath("/deck");
        updateDeckReq.setBody("[\"" + cardId1 + "\", \"" + cardId2 + "\", \"" + cardId3 + "\", \"" + invalidCardId + "\"]");
        updateDeckReq.setHeader("Authorization", "Bearer " + username + "-mtcgToken");
        Response updateDeckResp = userController.handle(updateDeckReq);
        assertEquals(Status.BAD_REQUEST, updateDeckResp.getStatus(), "Configuring a deck with invalid card ownership should return 400 BAD REQUEST");
        assertTrue(updateDeckResp.getBody().contains("Invalid cards or ownership issue"));
    }

    @Test
    public void testGetConfiguredDeckJSON() {
        String username = "deckUser4";
        // Registering user and add 4 cards
        Request regReq = new Request();
        regReq.setMethod(Method.POST);
        regReq.setPath("/users");
        regReq.setBody("{\"Username\":\"" + username + "\", \"Password\":\"pass\"}");
        Response regResp = userController.handle(regReq);
        assertEquals(Status.CREATED, regResp.getStatus(), "User registration should succeed");

        String cardId1 = "aaaaaaaa-0000-0000-0000-000000000001";
        String cardId2 = "aaaaaaaa-0000-0000-0000-000000000002";
        String cardId3 = "aaaaaaaa-0000-0000-0000-000000000003";
        String cardId4 = "aaaaaaaa-0000-0000-0000-000000000004";
        addCardForUser(username, cardId1, "WaterGoblin", 10.0);
        addCardForUser(username, cardId2, "Dragon", 50.0);
        addCardForUser(username, cardId3, "WaterSpell", 20.0);
        addCardForUser(username, cardId4, "Ork", 45.0);

        // Configure deck
        Request updateDeckReq = new Request();
        updateDeckReq.setMethod(Method.PUT);
        updateDeckReq.setPath("/deck");
        updateDeckReq.setBody("[\"" + cardId1 + "\", \"" + cardId2 + "\", \"" + cardId3 + "\", \"" + cardId4 + "\"]");
        updateDeckReq.setHeader("Authorization", "Bearer " + username + "-mtcgToken");
        Response updateDeckResp = userController.handle(updateDeckReq);
        assertEquals(Status.OK, updateDeckResp.getStatus(), "Deck configuration should succeed");

        // Retrieve deck in JSON format
        Request getDeckReq = new Request();
        getDeckReq.setMethod(Method.GET);
        getDeckReq.setPath("/deck"); // No query parameter -> JSON format
        getDeckReq.setHeader("Authorization", "Bearer " + username + "-mtcgToken");
        Response getDeckResp = userController.handle(getDeckReq);
        assertEquals(Status.OK, getDeckResp.getStatus(), "Retrieving deck should return 200 OK");
        // Check response for JSON representation of the 4 cards
        assertTrue(getDeckResp.getBody().contains("\"Id\":\"" + cardId1 + "\""), "Response should contain cardId1");
        assertTrue(getDeckResp.getBody().contains("\"Id\":\"" + cardId2 + "\""), "Response should contain cardId2");
        assertTrue(getDeckResp.getBody().contains("\"Id\":\"" + cardId3 + "\""), "Response should contain cardId3");
        assertTrue(getDeckResp.getBody().contains("\"Id\":\"" + cardId4 + "\""), "Response should contain cardId4");
    }
}
