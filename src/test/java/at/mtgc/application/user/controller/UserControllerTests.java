package at.mtgc.application.user.controller;

import at.mtgc.server.http.Method;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.application.user.service.UserService;
import at.mtgc.application.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTests {

    private UserController userController;

    @BeforeEach
    public void setUp() {
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        userController = new UserController(userService);
    }

    @Test
    public void testUserRegistrationSuccess() {
        // Simulating request for registering new user
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setPath("/users");
        request.setBody("{\"Username\":\"testuser\", \"Password\":\"password\"}");

        Response response = userController.handle(request);

        assertEquals(Status.CREATED, response.getStatus(), "User registration should return 201 CREATED");
        assertTrue(response.getBody().contains("User successfully created"), "Response should confirm successful registration");
    }

    @Test
    public void testUserRegistrationConflict() {
        // Register same user twice
        Request request1 = new Request();
        request1.setMethod(Method.POST);
        request1.setPath("/users");
        request1.setBody("{\"Username\":\"duplicateUser\", \"Password\":\"password\"}");
        Response response1 = userController.handle(request1);
        assertEquals(Status.CREATED, response1.getStatus(), "First registration should succeed");
        
        Request request2 = new Request();
        request2.setMethod(Method.POST);
        request2.setPath("/users");
        request2.setBody("{\"Username\":\"duplicateUser\", \"Password\":\"password\"}");
        Response response2 = userController.handle(request2);
        assertEquals(Status.CONFLICT, response2.getStatus(), "Duplicate registration should return 409 CONFLICT");
        assertTrue(response2.getBody().contains("User already exists"), "Response should indicate that the user already exists");
    }
}