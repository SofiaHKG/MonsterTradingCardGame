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

    // Test 1
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

    // Test 2
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

    // Test 3
    @Test
    public void testUserLoginSuccess() {
        // Register user "loginUser"
        Request registrationRequest = new Request();
        registrationRequest.setMethod(Method.POST);
        registrationRequest.setPath("/users");
        registrationRequest.setBody("{\"Username\":\"loginUser\", \"Password\":\"secret\"}");
        Response regResponse = userController.handle(registrationRequest);
        assertEquals(Status.CREATED, regResponse.getStatus(), "Registration should return 201 Created");

        // Login with correct credentials
        Request loginRequest = new Request();
        loginRequest.setMethod(Method.POST);
        loginRequest.setPath("/sessions");
        loginRequest.setBody("{\"Username\":\"loginUser\", \"Password\":\"secret\"}");
        Response loginResponse = userController.handle(loginRequest);
        assertEquals(Status.OK, loginResponse.getStatus(), "Login should return 200 OK");
        String expectedToken = "loginUser-mtcgToken";
        assertTrue(loginResponse.getBody().contains(expectedToken), "Token should be " + expectedToken);
    }

    // Test 4
    @Test
    public void testUserLoginFailure() {
        // Register user "wrongPassUser"
        Request registrationRequest = new Request();
        registrationRequest.setMethod(Method.POST);
        registrationRequest.setPath("/users");
        registrationRequest.setBody("{\"Username\":\"wrongPassUser\", \"Password\":\"correct\"}");
        Response regResponse = userController.handle(registrationRequest);
        assertEquals(Status.CREATED, regResponse.getStatus(), "Registration should succeed");

        // Attempt to login with wrong password
        Request loginRequest = new Request();
        loginRequest.setMethod(Method.POST);
        loginRequest.setPath("/sessions");
        loginRequest.setBody("{\"Username\":\"wrongPassUser\", \"Password\":\"incorrect\"}");
        Response loginResponse = userController.handle(loginRequest);
        assertEquals(Status.UNAUTHORIZED, loginResponse.getStatus(), "Login with wrong password should return 401 Unauthorized");
    }

    // Test 5
    @Test
    public void testTokenUpdateOnLogin() {
        // Registering user "tokenUser"
        Request registrationRequest = new Request();
        registrationRequest.setMethod(Method.POST);
        registrationRequest.setPath("/users");
        registrationRequest.setBody("{\"Username\":\"tokenUser\", \"Password\":\"mypassword\"}");
        Response regResponse = userController.handle(registrationRequest);
        assertEquals(Status.CREATED, regResponse.getStatus(), "Registration should return 201 Created");

        // Login "tokenUser" and check for token
        Request loginRequest = new Request();
        loginRequest.setMethod(Method.POST);
        loginRequest.setPath("/sessions");
        loginRequest.setBody("{\"Username\":\"tokenUser\", \"Password\":\"mypassword\"}");
        Response loginResponse = userController.handle(loginRequest);
        assertEquals(Status.OK, loginResponse.getStatus(), "Login should return 200 OK");
        String expectedToken = "tokenUser-mtcgToken";
        assertTrue(loginResponse.getBody().contains(expectedToken), "Token should be " + expectedToken);
    }

    // Test 6
    @Test
    public void testGetOwnUserDataSuccess() {
        // Register and login user "dataUser"
        Request registrationRequest = new Request();
        registrationRequest.setMethod(Method.POST);
        registrationRequest.setPath("/users");
        registrationRequest.setBody("{\"Username\":\"dataUser\", \"Password\":\"dataPass\"}");
        Response regResponse = userController.handle(registrationRequest);
        assertEquals(Status.CREATED, regResponse.getStatus(), "Registration should return 201 Created");

        Request loginRequest = new Request();
        loginRequest.setMethod(Method.POST);
        loginRequest.setPath("/sessions");
        loginRequest.setBody("{\"Username\":\"dataUser\", \"Password\":\"dataPass\"}");
        Response loginResponse = userController.handle(loginRequest);
        assertEquals(Status.OK, loginResponse.getStatus(), "Login should return 200 OK");
        String token = loginResponse.getBody().trim();

        // Retrieving own data via GET /users/dataUser
        Request getRequest = new Request();
        getRequest.setMethod(Method.GET);
        getRequest.setPath("/users/dataUser");
        getRequest.setHeader("Authorization", "Bearer " + token);
        Response getResponse = userController.handle(getRequest);
        assertEquals(Status.OK, getResponse.getStatus(), "Fetching own user data should return 200 OK");
        assertTrue(getResponse.getBody().contains("dataUser"), "Response should contain the username 'dataUser'");
    }
}