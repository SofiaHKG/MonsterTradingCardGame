package at.mtgc.application.packages.controller;

import at.mtgc.server.http.Method;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.server.util.DatabaseManager;
import at.mtgc.application.packages.repository.PackageRepository;
import at.mtgc.application.packages.service.PackageService;
import at.mtgc.application.user.controller.UserController;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.application.user.service.UserService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PackageControllerTests {

    private PackageController packageController;

    @BeforeEach
    public void setUp() {
        PackageRepository packageRepository = new PackageRepository();
        PackageService packageService = new PackageService(packageRepository);
        packageController = new PackageController(packageService);
    }

    // Test 10
    @Test
    public void testCreatePackageSuccess() {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setPath("/packages");
        // JSON array with five cards and valid UUID-Strings
        String jsonBody = "["
                + "{\"Id\":\"11111111-1111-1111-1111-111111111111\", \"Name\":\"WaterGoblin\", \"Damage\":10.0},"
                + "{\"Id\":\"22222222-2222-2222-2222-222222222222\", \"Name\":\"Dragon\", \"Damage\":50.0},"
                + "{\"Id\":\"33333333-3333-3333-3333-333333333333\", \"Name\":\"WaterSpell\", \"Damage\":20.0},"
                + "{\"Id\":\"44444444-4444-4444-4444-444444444444\", \"Name\":\"Ork\", \"Damage\":45.0},"
                + "{\"Id\":\"55555555-5555-5555-5555-555555555555\", \"Name\":\"FireSpell\", \"Damage\":25.0}"
                + "]";
        request.setBody(jsonBody);
        // Only admin has permission to create packages
        request.setHeader("Authorization", "Bearer admin-mtcgToken");

        Response response = packageController.handle(request);
        assertEquals(Status.CREATED, response.getStatus(), "Package creation should return 201 CREATED");
        assertTrue(response.getBody().contains("Package created successfully"), "Response should confirm package creation");
    }

    // Test 11
    @Test
    public void testCreatePackageWrongCardCount() {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setPath("/packages");
        // JSON array with only 2 instead of 5 cards
        String jsonBody = "["
                + "{\"Id\":\"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa\", \"Name\":\"WaterGoblin\", \"Damage\":10.0},"
                + "{\"Id\":\"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb\", \"Name\":\"Dragon\", \"Damage\":50.0}"
                + "]";
        request.setBody(jsonBody);
        request.setHeader("Authorization", "Bearer admin-mtcgToken");

        Response response = packageController.handle(request);
        assertEquals(Status.BAD_REQUEST, response.getStatus(), "Creating a package with wrong card count should return 400 BAD REQUEST");
        assertTrue(response.getBody().contains("A package must contain exactly 5 cards"), "Response should indicate card count error");
    }

    // Test 12
    @Test
    public void testAcquirePackageSuccess() {
        // Register "acquireUser"
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        UserController userController = new UserController(userService);
        Request regUserReq = new Request();
        regUserReq.setMethod(Method.POST);
        regUserReq.setPath("/users");
        regUserReq.setBody("{\"Username\":\"acquireUser\", \"Password\":\"acquirePass\"}");
        Response regUserResp = userController.handle(regUserReq);
        assertEquals(Status.CREATED, regUserResp.getStatus(), "User registration for acquireUser should succeed");

        // Create package
        Request createRequest = new Request();
        createRequest.setMethod(Method.POST);
        createRequest.setPath("/packages");
        String jsonBody = "["
                + "{\"Id\":\"66666666-6666-6666-6666-666666666666\", \"Name\":\"WaterGoblin\", \"Damage\":10.0},"
                + "{\"Id\":\"77777777-7777-7777-7777-777777777777\", \"Name\":\"Dragon\", \"Damage\":50.0},"
                + "{\"Id\":\"88888888-8888-8888-8888-888888888888\", \"Name\":\"WaterSpell\", \"Damage\":20.0},"
                + "{\"Id\":\"99999999-9999-9999-9999-999999999999\", \"Name\":\"Ork\", \"Damage\":45.0},"
                + "{\"Id\":\"00000000-0000-0000-0000-000000000000\", \"Name\":\"FireSpell\", \"Damage\":25.0}"
                + "]";
        createRequest.setBody(jsonBody);
        createRequest.setHeader("Authorization", "Bearer admin-mtcgToken");
        Response createResponse = packageController.handle(createRequest);
        assertEquals(Status.CREATED, createResponse.getStatus(), "Package creation should succeed for acquisition test");

        // Successfull acquisition of packet by "acquireUser"
        Request acquireRequest = new Request();
        acquireRequest.setMethod(Method.POST);
        acquireRequest.setPath("/transactions/packages");
        acquireRequest.setHeader("Authorization", "Bearer acquireUser-mtcgToken");
        acquireRequest.setBody("");
        Response acquireResponse = packageController.handle(acquireRequest);
        assertEquals(Status.CREATED, acquireResponse.getStatus(), "Package acquisition should return 201 CREATED");
        assertTrue(acquireResponse.getBody().contains("Package acquired successfully"), "Response should confirm package acquisition");
    }

    // Test 13
    @Test
    public void testAcquirePackageFailure() {
        // Register "noPackageUser"
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        UserController userController = new UserController(userService);
        Request regUserReq = new Request();
        regUserReq.setMethod(Method.POST);
        regUserReq.setPath("/users");
        regUserReq.setBody("{\"Username\":\"noPackageUser\", \"Password\":\"noPackagePass\"}");
        Response regUserResp = userController.handle(regUserReq);
        assertEquals(Status.CREATED, regUserResp.getStatus(), "User registration for noPackageUser should succeed");

        // Set coins of noPackageUser to 0 to simulate not having enough coins for packet acquisition
        setUserCoinsToZero("noPackageUser");

        Request acquireRequest = new Request();
        acquireRequest.setMethod(Method.POST);
        acquireRequest.setPath("/transactions/packages");
        acquireRequest.setHeader("Authorization", "Bearer noPackageUser-mtcgToken");
        acquireRequest.setBody("");
        Response acquireResponse = packageController.handle(acquireRequest);
        assertEquals(Status.BAD_REQUEST, acquireResponse.getStatus(), "Package acquisition should fail when insufficient coins");
        assertTrue(acquireResponse.getBody().contains("Not enough coins or no package available"), "Response should indicate insufficient coins or no package");
    }

    // Helper method to set coins to 0
    private void setUserCoinsToZero(String username) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET coins = 0 WHERE username = ?")) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            fail("Failed to update coins for user " + username + ": " + e.getMessage());
        }
    }
}