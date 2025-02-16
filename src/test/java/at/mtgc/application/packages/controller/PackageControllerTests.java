package at.mtgc.application.packages.controller;

import at.mtgc.server.http.Method;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.application.packages.repository.PackageRepository;
import at.mtgc.application.packages.service.PackageService;
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
}
