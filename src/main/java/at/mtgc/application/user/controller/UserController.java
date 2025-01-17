package at.mtgc.application.user.controller;

import at.mtgc.server.Application;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.application.user.entity.User;
import at.mtgc.application.user.service.UserService;
import at.mtgc.application.packages.entity.Card;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class UserController implements Application {
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService) {
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Response handle(Request request) {
        System.out.println("Received request: " + request.getMethod() + " " + request.getPath()); // Debug

        Response response = new Response();

        try {
            switch(request.getMethod()) {
                case POST -> {
                    if(request.getPath().equals("/users")) {
                        return handleUserRegistration(request);
                    } else if(request.getPath().equals("/sessions")) {
                        return handleUserLogin(request);
                    }
                }
                case GET -> {
                    if(request.getPath().startsWith("/users/")) {
                        return handleGetUser(request);
                    } else if(request.getPath().equals("/cards")) {
                        return handleGetCards(request);
                    } else if(request.getPath().equals("/deck")) {
                        return handleGetDeck(request);
                    }
                }
                case PUT -> {
                    if (request.getPath().equals("/deck")) {
                        return handleUpdateDeck(request);
                    } else if(request.getPath().startsWith("/users/")) {
                        return handleUpdateUser(request);
                    }
                }
                default -> {
                    response.setStatus(Status.METHOD_NOT_ALLOWED);
                    response.setBody("Method not allowed\n");
                }
            }
        } catch(IOException e) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("Error processing request: " + e.getMessage());
        }

        return response;
    }

    private Response handleGetUser(Request request) {
        Response response = new Response();
        System.out.println("Processing GET user request: " + request.getPath()); // Debug

        // Username extrahieren
        String[] parts = request.getPath().split("/");
        if(parts.length < 3) {
            response.setStatus(Status.BAD_REQUEST);
            response.setBody("Invalid username\n");
            return response;
        }

        String username = parts[2];
        System.out.println("Extracted username: " + username); // Debug

        User user = userService.getUserByUsername(username);
        if(user == null) {
            response.setStatus(Status.NOT_FOUND);
            response.setBody("User not found\n");
            return response;
        }

        response.setStatus(Status.OK);
        response.setHeader("Content-Type", "application/json");
        response.setBody("{\"username\":\"" + user.getUsername() + "\", \"token\":\"" + user.getToken() + "\", \"coins\": " + user.getCoins() + "}");
        return response;
    }

    private Response handleGetCards(Request request) {
        System.out.println("Handling GET /cards request"); // Debug

        Response response = new Response();
        String token = request.getHeader("Authorization");

        if(token == null || !token.startsWith("Bearer ")) {
            response.setStatus(Status.UNAUTHORIZED);
            response.setBody("{\"message\":\"Missing or invalid token\"}");
            return response;
        }

        String username = token.replace("Bearer ", "").replace("-mtcgToken", "");
        System.out.println("Fetching cards for user: " + username); // Debugging

        List<Card> cards = userService.getUserCards(username);
        if(cards.isEmpty()) {
            System.out.println("No cards found for user: " + username); // Debugging
            response.setStatus(Status.NOT_FOUND);
            response.setBody("{\"message\":\"No cards found for user\"}");
            return response;
        }

        response.setStatus(Status.OK);
        response.setHeader("Content-Type", "application/json");

        try {
            response.setBody(objectMapper.writeValueAsString(cards));
        } catch(IOException e) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("{\"message\":\"Error serializing cards\"}");
        }

        return response;
    }

    private Response handleUserRegistration(Request request) throws IOException {
        Response response = new Response();
        User user = parseUser(request);

        System.out.println("Registering user: " + user.getUsername()); // Debugging-Log

        if(userService.register(user)) {
            response.setStatus(Status.CREATED);
            response.setBody("User successfully created\n");
        } else {
            response.setStatus(Status.CONFLICT);
            response.setBody("User already exists\n");
        }
        return response;
    }

    private Response handleUserLogin(Request request) throws IOException {
        Response response = new Response();
        User user = parseUser(request);

        System.out.println("Attempting login for user: " + user.getUsername()); // Debugging-Log

        User loggedInUser = userService.login(user.getUsername(), user.getPassword());
        if(loggedInUser != null) {
            response.setStatus(Status.OK);
            response.setBody(loggedInUser.getUsername() + "-mtcgToken\n");
        } else {
            response.setStatus(Status.UNAUTHORIZED);
            response.setBody("Login failed\n");
        }
        return response;
    }

    private Response handleGetDeck(Request request) {
        Response response = new Response();
        String token = request.getHeader("Authorization");

        if(token == null || !token.startsWith("Bearer ")) {
            response.setStatus(Status.UNAUTHORIZED);
            response.setBody("{\"message\":\"Missing or invalid token\"}");
            return response;
        }

        String username = token.replace("Bearer ", "").replace("-mtcgToken", "");
        System.out.println("Fetching deck for user: " + username); // Debugging

        List<Card> deck = userService.getUserDeck(username);
        // Unabhängig davon, ob das Deck leer ist oder nicht, geben wir HTTP 200 zurück:
        response.setStatus(Status.OK);
        response.setHeader("Content-Type", "application/json");

        try {
            response.setBody(objectMapper.writeValueAsString(deck));
        } catch(IOException e) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("{\"message\":\"Error serializing deck\"}");
        }

        return response;
    }

    private Response handleUpdateDeck(Request request) {
        Response response = new Response();
        String token = request.getHeader("Authorization");

        if(token == null || !token.startsWith("Bearer ")) {
            response.setStatus(Status.UNAUTHORIZED);
            response.setBody("{\"message\":\"Missing or invalid token\"}");
            return response;
        }

        String username = token.replace("Bearer ", "").replace("-mtcgToken", "");
        System.out.println("Updating deck for user: " + username); // Debugging

        try {
            // Parse JSON-Array from Request-Body
            System.out.println("Request Body: " + request.getBody());
            List<String> cardIds = objectMapper.readValue(request.getBody(), List.class);

            if(cardIds.size() != 4) {
                response.setStatus(Status.BAD_REQUEST);
                response.setBody("{\"message\":\"Deck must contain exactly 4 cards\"}");
                return response;
            }

            boolean success = userService.updateUserDeck(username, cardIds);
            if(!success) {
                response.setStatus(Status.BAD_REQUEST);
                response.setBody("{\"message\":\"Invalid cards or ownership issue\"}");
                return response;
            }

            response.setStatus(Status.OK);
            response.setBody("{\"message\":\"Deck updated successfully\"}");
        } catch(IOException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage()); // Debugging
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("{\"message\":\"Error processing request\"}");
        }

        return response;
    }

    private Response handleUpdateUser(Request request) {
        System.out.println("Processing PUT /users request for: " + request.getPath()); // Debug
        System.out.println("Request Body: " + request.getBody()); // Debug

        Response response = new Response();
        String token = request.getHeader("Authorization");

        if(token == null || !token.startsWith("Bearer ")) {
            System.out.println("Authorization failed - Missing or invalid token"); // Debug
            response.setStatus(Status.UNAUTHORIZED);
            response.setBody("{\"message\":\"Missing or invalid token\"}");
            return response;
        }

        String username = token.replace("Bearer ", "").replace("-mtcgToken", "");
        String[] parts = request.getPath().split("/");

        if(parts.length < 3) {
            System.out.println("Invalid request format"); // Debug
            response.setStatus(Status.BAD_REQUEST);
            response.setBody("{\"message\":\"Invalid request format\"}");
            return response;
        }

        String targetUsername = parts[2];

        // Überprüfen, ob der Nutzer sich selbst bearbeitet
        if(!username.equals(targetUsername)) {
            System.out.println("Forbidden: User " + username + " tried to update " + targetUsername); // Debug
            response.setStatus(Status.FORBIDDEN);
            response.setBody("{\"message\":\"You can only update your own profile\"}");
            return response;
        }

        try {
            System.out.println("Parsing JSON request body..."); // Debug
            User updatedUser = objectMapper.readValue(request.getBody(), User.class);
            System.out.println("Parsed User Object: " + updatedUser); // Debug

            boolean success = userService.updateUser(targetUsername, updatedUser);

            if(!success) {
                System.out.println("User update failed in database"); // Debug
                response.setStatus(Status.BAD_REQUEST);
                response.setBody("{\"message\":\"Failed to update user\"}");
                return response;
            }

            response.setStatus(Status.OK);
            response.setBody("{\"message\":\"User updated successfully\"}");
        } catch(IOException e) {
            System.out.println("Exception: " + e.getMessage()); // Debug
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("{\"message\":\"Error processing request\"}");
        }

        return response;
    }

    private User parseUser(Request request) throws IOException {
        return objectMapper.readValue(request.getBody(), User.class);
    }
}