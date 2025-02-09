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

    // Test 7
    @Test
    public void testGetOtherUserDataForbidden() {
        // Register two users: userA and userB
        Request regA = new Request();
        regA.setMethod(Method.POST);
        regA.setPath("/users");
        regA.setBody("{\"Username\":\"userA\", \"Password\":\"passA\"}");
        Response resA = userController.handle(regA);
        assertEquals(Status.CREATED, resA.getStatus(), "Registration of userA should succeed");

        Request regB = new Request();
        regB.setMethod(Method.POST);
        regB.setPath("/users");
        regB.setBody("{\"Username\":\"userB\", \"Password\":\"passB\"}");
        Response resB = userController.handle(regB);
        assertEquals(Status.CREATED, resB.getStatus(), "Registration of userB should succeed");

        // Login as userA
        Request loginA = new Request();
        loginA.setMethod(Method.POST);
        loginA.setPath("/sessions");
        loginA.setBody("{\"Username\":\"userA\", \"Password\":\"passA\"}");
        Response loginAResponse = userController.handle(loginA);
        assertEquals(Status.OK, loginAResponse.getStatus(), "Login of userA should succeed");
        String tokenA = loginAResponse.getBody().trim();

        // userA tries to retrieve userB data
        Request getUserB = new Request();
        getUserB.setMethod(Method.GET);
        getUserB.setPath("/users/userB");
        getUserB.setHeader("Authorization", "Bearer " + tokenA);
        Response getUserBResponse = userController.handle(getUserB);
        assertEquals(Status.FORBIDDEN, getUserBResponse.getStatus(), "UserA should not be allowed to access userB's data");
    }

    // Test 8
    @Test
    public void testUpdateOwnProfileSuccess() {
        // Register and login user "updateUser"
        Request reg = new Request();
        reg.setMethod(Method.POST);
        reg.setPath("/users");
        reg.setBody("{\"Username\":\"updateUser\", \"Password\":\"updatePass\"}");
        Response regResponse = userController.handle(reg);
        assertEquals(Status.CREATED, regResponse.getStatus(), "Registration should return 201 Created");

        Request login = new Request();
        login.setMethod(Method.POST);
        login.setPath("/sessions");
        login.setBody("{\"Username\":\"updateUser\", \"Password\":\"updatePass\"}");
        Response loginResponse = userController.handle(login);
        assertEquals(Status.OK, loginResponse.getStatus(), "Login should return 200 OK");
        String token = loginResponse.getBody().trim();

        // Updating own user profile via PUT /users/updateUser
        Request updateReq = new Request();
        updateReq.setMethod(Method.PUT);
        updateReq.setPath("/users/updateUser");
        updateReq.setHeader("Authorization", "Bearer " + token);
        // Send new data (Name, Bio, Image)
        updateReq.setBody("{\"username\":\"updateUser\", \"password\":\"updatePass\", \"Name\":\"New Name\", \"Bio\":\"New Bio\", \"Image\":\":-)\"}");
        Response updateResp = userController.handle(updateReq);
        assertEquals(Status.OK, updateResp.getStatus(), "Profile update should return 200 OK");
        assertTrue(updateResp.getBody().contains("User updated successfully"), "Response should confirm profile update");
    }

    // Test 9
    @Test
    public void testUpdateOtherProfileForbidden() {
        // Register two users: userX und userY
        Request regX = new Request();
        regX.setMethod(Method.POST);
        regX.setPath("/users");
        regX.setBody("{\"Username\":\"userX\", \"Password\":\"passX\"}");
        Response resX = userController.handle(regX);
        assertEquals(Status.CREATED, resX.getStatus(), "Registration of userX should succeed");

        Request regY = new Request();
        regY.setMethod(Method.POST);
        regY.setPath("/users");
        regY.setBody("{\"Username\":\"userY\", \"Password\":\"passY\"}");
        Response resY = userController.handle(regY);
        assertEquals(Status.CREATED, resY.getStatus(), "Registration of userY should succeed");

        // Login as userX
        Request loginX = new Request();
        loginX.setMethod(Method.POST);
        loginX.setPath("/sessions");
        loginX.setBody("{\"Username\":\"userX\", \"Password\":\"passX\"}");
        Response loginXResponse = userController.handle(loginX);
        assertEquals(Status.OK, loginXResponse.getStatus(), "Login of userX should succeed");
        String tokenX = loginXResponse.getBody().trim();

        // userX attempts to update userY profile
        Request updateReq = new Request();
        updateReq.setMethod(Method.PUT);
        updateReq.setPath("/users/userY");
        updateReq.setHeader("Authorization", "Bearer " + tokenX);
        updateReq.setBody("{\"username\":\"userY\", \"password\":\"passY\", \"Name\":\"Hacked Name\", \"Bio\":\"Hacked Bio\", \"Image\":\":-(\"}");
        Response updateResp = userController.handle(updateReq);
        assertEquals(Status.FORBIDDEN, updateResp.getStatus(), "Updating another user's profile should be forbidden");
        assertTrue(updateResp.getBody().contains("You can only update your own profile"), "Response should indicate that profile update is forbidden");
    }
}