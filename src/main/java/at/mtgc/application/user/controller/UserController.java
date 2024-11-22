package at.mtgc.application.user.controller;

import at.mtgc.server.Application;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.application.user.entity.User;
import at.mtgc.application.user.service.UserService;

public class UserController implements Application {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Response handle(Request request) {
        Response response = new Response();

        switch (request.getMethod()) {
            case POST -> {
                if (request.getPath().equals("/users")) {
                    User user = parseUser(request);
                    if (userService.register(user)) {
                        response.setStatus(Status.CREATED);
                    } else {
                        response.setStatus(Status.CONFLICT);
                    }
                } else if (request.getPath().equals("/sessions")) {
                    User user = parseUser(request);
                    User loggedInUser = userService.login(user.getUsername(), user.getPassword());
                    if (loggedInUser != null) {
                        response.setStatus(Status.OK);
                        response.setBody(loggedInUser.getUsername() + "-mtcgToken");
                    } else {
                        response.setStatus(Status.UNAUTHORIZED);
                    }
                }
            }
            default -> response.setStatus(Status.METHOD_NOT_ALLOWED);
        }

        return response;
    }

    private User parseUser(Request request) {
        String[] parts = request.getBody().replace("{", "").replace("}", "").split(",");
        String username = parts[0].split(":")[1].replace("\"", "").trim();
        String password = parts[1].split(":")[1].replace("\"", "").trim();
        return new User(username, password);
    }
}
