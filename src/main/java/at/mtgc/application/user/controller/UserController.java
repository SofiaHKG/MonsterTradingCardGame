package at.mtgc.application.user.controller;

import at.mtgc.server.Application;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.application.user.entity.User;
import at.mtgc.application.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class UserController implements Application {
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService) {
        this.userService = userService;
        this.objectMapper = new ObjectMapper(); // JSON-Verarbeitung
    }

    @Override
    public Response handle(Request request) {
        Response response = new Response();

        try {
            switch (request.getMethod()) {
                case POST -> {
                    if (request.getPath().equals("/users")) {
                        User user = parseUser(request);
                        if (userService.register(user)) {
                            response.setStatus(Status.CREATED);
                        } else {
                            response.setStatus(Status.CONFLICT);
                            response.setBody("User already exists.");
                        }
                    } else if (request.getPath().equals("/sessions")) {
                        User user = parseUser(request);
                        User loggedInUser = userService.login(user.getUsername(), user.getPassword());
                        if (loggedInUser != null) {
                            response.setStatus(Status.OK);
                            response.setBody(loggedInUser.getUsername() + "-mtcgToken");
                        } else {
                            response.setStatus(Status.UNAUTHORIZED);
                            response.setBody("Invalid username or password.");
                        }
                    }
                }
                default -> {
                    response.setStatus(Status.METHOD_NOT_ALLOWED);
                    response.setBody("Method not allowed.");
                }
            }
        } catch (IOException e) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("Error processing request: " + e.getMessage());
        }

        return response;
    }

    private User parseUser(Request request) throws IOException {
        return objectMapper.readValue(request.getBody(), User.class);
    }
}
