package at.mtgc.application.packages.controller;

import at.mtgc.application.packages.entity.Card;
import at.mtgc.application.packages.entity.Package;
import at.mtgc.application.packages.service.PackageService;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class PackageController implements at.mtgc.server.Application {
    private final PackageService packageService;
    private final ObjectMapper objectMapper;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Response handle(Request request) {
        Response response = new Response();

        if("POST".equals(request.getMethod().toString()) && "/packages".equals(request.getPath())) {
            return handleCreatePackage(request);
        }

        if("POST".equals(request.getMethod().toString()) && "/transactions/packages".equals(request.getPath())) {
            return handleAcquirePackage(request);
        }

        response.setStatus(Status.METHOD_NOT_ALLOWED);
        response.setHeader("Content-Type", "text/plain");
        response.setBody("Method not allowed");
        return response;
    }

    private Response handleCreatePackage(Request request) {
        Response response = new Response();

        String token = request.getHeader("Authorization");
        if(token == null || !token.equals("Bearer admin-mtcgToken")) {
            response.setStatus(Status.UNAUTHORIZED);
            response.setBody("{\"message\":\"Unauthorized: Only admin can create packages\"}");
            return response;
        }

        try {
            List<Card> cards = objectMapper.readValue(request.getBody(), new TypeReference<List<Card>>() {});

            if(cards.size() != 5) {
                response.setStatus(Status.BAD_REQUEST);
                response.setBody("{\"message\":\"A package must contain exactly 5 cards\"}");
                return response;
            }

            Package pack = new Package(cards);
            packageService.addPackage(pack);

            response.setStatus(Status.CREATED);
            response.setHeader("Content-Type", "application/json");
            response.setBody("{\"message\":\"Package created successfully\"}");
        } catch(IOException e) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("{\"message\":\"Error processing request: " + e.getMessage() + "\"}");
        }

        return response;
    }

    private Response handleAcquirePackage(Request request) {
        System.out.println("Processing package acquisition request: " + request.getPath()); // Debug
        Response response = new Response();

        String token = request.getHeader("Authorization");
        if(token == null || !token.startsWith("Bearer ")) {
            response.setStatus(Status.UNAUTHORIZED);
            response.setBody("{\"message\":\"Missing or invalid token\"}");
            return response;
        }

        String username = token.replace("Bearer ", "").replace("-mtcgToken", "");

        if(packageService.acquirePackage(username)) {
            response.setStatus(Status.OK);
            response.setBody("{\"message\":\"Package acquired successfully\"}");
        } else {
            response.setStatus(Status.BAD_REQUEST);
            response.setBody("{\"message\":\"Not enough coins or no package available\"}");
        }

        return response;
    }
}