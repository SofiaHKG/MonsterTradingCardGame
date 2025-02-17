package at.mtgc.application.user.controller;

import at.mtgc.server.http.Method;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.server.util.DatabaseManager;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.application.user.service.UserService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeckConfigurationTests {

    private UserController userController;

    @BeforeEach
    public void setUp() {
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        userController = new UserController(userService);
    }

    // Helper method to insert a card for a user into the database
    private void addCardForUser(String username, String cardId, String name, double damage) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO cards (id, name, damage, owner, in_deck) VALUES (?, ?, ?, ?, false)"
             )) {
            stmt.setObject(1, UUID.fromString(cardId));
            stmt.setString(2, name);
            stmt.setDouble(3, damage);
            stmt.setString(4, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            fail("Failed to add card for user " + username + ": " + e.getMessage());
        }
    }

    // Test 14
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
        assertTrue(updateDeckResp.getBody().contains("Deck updated successfully"), "Response should confirm deck update");
    }

    // Test 15
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
        assertTrue(updateDeckResp.getBody().contains("Deck must contain exactly 4 cards"), "Response should indicate incorrect deck size");
    }
}
