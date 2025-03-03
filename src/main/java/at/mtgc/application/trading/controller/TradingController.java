package at.mtgc.application.trading.controller;

import at.mtgc.server.Application;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.application.trading.entity.TradingDeal;
import at.mtgc.application.trading.service.TradingService;
import at.mtgc.server.http.HttpException;
import at.mtgc.server.http.Method;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


public class TradingController implements Application {

    private final TradingService tradingService;
    private final ObjectMapper objectMapper;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Response handle(Request request) {
        String path = request.getPath();  // "/tradings" or "/tradings/6cd85277-4590-49d4-b0cf-ba0a921faad0"
        Method method = request.getMethod();

        if(path.equals("/tradings") && method == Method.GET) {
            return handleGetAllDeals(request);
        }

        if(path.equals("/tradings") && method == Method.POST) {
            return handleCreateDeal(request);
        }

        if(path.startsWith("/tradings/") && method == Method.DELETE) {
            return handleDeleteDeal(request);
        }

        if(path.startsWith("/tradings/") && method == Method.POST) {
            return handleExecuteDeal(request);
        }

        // FAllback 404
        Response resp = new Response();
        resp.setStatus(Status.NOT_FOUND);
        resp.setBody("Not Found");
        return resp;
    }

    private Response handleGetAllDeals(Request request) {
        Response response = new Response();

        // token check
        String username = checkToken(request, response);
        if(username == null) {
            return response;  // UNAUTHORIZED
        }

        List<TradingDeal> deals = tradingService.getAllDeals();
        response.setStatus(Status.OK);
        response.setHeader("Content-Type", "application/json");
        try {
            if(deals.isEmpty()) {
                response.setBody("[]");
            } else {
                response.setBody(objectMapper.writeValueAsString(deals));
            }
        } catch (IOException e) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("{\"message\":\"Error serializing trading deals\"}");
        }
        return response;
    }

    private Response handleCreateDeal(Request request) {
        Response response = new Response();
        String username = checkToken(request, response);
        if(username == null) {
            return response;
        }

        try {
            TradingDeal deal = objectMapper.readValue(request.getBody(), TradingDeal.class);
            // createDeal can throw HttpException
            tradingService.createDeal(username, deal);

            response.setStatus(Status.CREATED);
            response.setBody("{\"message\":\"Trading deal successfully created\"}");

        } catch(IOException e) {
            response.setStatus(Status.BAD_REQUEST);
            response.setBody("{\"message\":\"Invalid JSON payload\"}");
        } catch(HttpException he) {
            response.setStatus(he.getStatus());
            response.setBody("{\"message\":\"" + he.getMessage() + "\"}");
        } catch(SQLException se) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("{\"message\":\"SQL Error: " + se.getMessage() + "\"}");
        }
        return response;
    }

    private Response handleDeleteDeal(Request request) {
        Response response = new Response();
        String username = checkToken(request, response);
        if(username == null) {
            return response;
        }

        // Parse ID from path
        String[] parts = request.getPath().split("/");
        if(parts.length < 3) {
            response.setStatus(Status.BAD_REQUEST);
            response.setBody("{\"message\":\"Missing dealId\"}");
            return response;
        }
        String dealId = parts[2];

        try {
            tradingService.deleteDeal(username, dealId);
            response.setStatus(Status.OK);
            response.setBody("{\"message\":\"Deal successfully deleted\"}");
        } catch(HttpException he) {
            response.setStatus(he.getStatus());
            response.setBody("{\"message\":\"" + he.getMessage() + "\"}");
        } catch(SQLException se) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("{\"message\":\"SQL Error: " + se.getMessage() + "\"}");
        }
        return response;
    }

    private Response handleExecuteDeal(Request request) {
        Response response = new Response();
        String username = checkToken(request, response);
        if(username == null) {
            return response;
        }

        String[] parts = request.getPath().split("/");
        if(parts.length < 3) {
            response.setStatus(Status.BAD_REQUEST);
            response.setBody("{\"message\":\"Missing dealId in path\"}");
            return response;
        }
        String dealId = parts[2];

        try {
            String offeredCardId = objectMapper.readValue(request.getBody(), String.class);

            tradingService.executeTrade(username, dealId, offeredCardId);

            response.setStatus(Status.CREATED);
            response.setBody("{\"message\":\"Trade executed successfully\"}");

        } catch(IOException e) {
            response.setStatus(Status.BAD_REQUEST);
            response.setBody("{\"message\":\"Invalid JSON payload\"}");
        } catch(HttpException he) {
            response.setStatus(he.getStatus());
            response.setBody("{\"message\":\"" + he.getMessage() + "\"}");
        } catch(SQLException se) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("{\"message\":\"SQL Error: " + se.getMessage() + "\"}");
        }
        return response;
    }

    // Token-Check
    private String checkToken(Request request, Response response) {
        String token = request.getHeader("Authorization");
        if(token == null || !token.startsWith("Bearer ")) {
            response.setStatus(Status.UNAUTHORIZED);
            response.setBody("{\"message\":\"Missing or invalid token\"}");
            return null;
        }
        return token.replace("Bearer ", "").replace("-mtcgToken", "");
    }
}
