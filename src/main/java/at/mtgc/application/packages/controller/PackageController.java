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

        if ("POST".equals(request.getMethod().toString())) {
            try {
                // Deserialize JSON to List<Card>
                List<Card> cards = objectMapper.readValue(request.getBody(), new TypeReference<List<Card>>() {});

                // Create a new Package
                Package pack = new Package(cards);
                packageService.addPackage(pack);

                response.setStatus(Status.CREATED);
                response.setHeader("Content-Type", "application/json");
                response.setBody("{\"message\":\"Package created successfully\"}");
            } catch (IOException e) {
                response.setStatus(Status.INTERNAL_SERVER_ERROR);
                response.setHeader("Content-Type", "text/plain");
                response.setBody("Error processing request: " + e.getMessage());
            }
        } else {
            response.setStatus(Status.METHOD_NOT_ALLOWED);
            response.setHeader("Content-Type", "text/plain");
            response.setBody("Method not allowed");
        }

        return response;
    }
}
