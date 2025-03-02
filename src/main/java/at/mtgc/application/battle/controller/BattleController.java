package at.mtgc.application.battle.controller;

import at.mtgc.server.Application;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Method;
import at.mtgc.server.http.Status;
import at.mtgc.server.http.HttpException;
import at.mtgc.application.battle.service.BattleService;

public class BattleController implements Application {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @Override
    public Response handle(Request request) {
        // POST /battles
        if("/battles".equals(request.getPath()) && request.getMethod() == Method.POST) {
            return handleStartBattle(request);
        }

        // 404 for everything else
        Response resp = new Response();
        resp.setStatus(Status.NOT_FOUND);
        resp.setBody("404 Not Found");
        return resp;
    }

    private Response handleStartBattle(Request request) {
        Response response = new Response();

        // Token check
        String token = request.getHeader("Authorization");
        if(token == null || !token.startsWith("Bearer ")) {
            response.setStatus(Status.UNAUTHORIZED);
            response.setBody("Missing or invalid token");
            return response;
        }
        String username = token.replace("Bearer ", "").replace("-mtcgToken","");

        String battleLog;
        try {
            battleLog = battleService.initiateBattle(username);
        } catch(HttpException httpexc) {
            response.setStatus(httpexc.getStatus());
            response.setBody(httpexc.getMessage());
            return response;
        } catch(Exception e) {
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            response.setBody("Error in battle: " + e.getMessage());
            return response;
        }

        // Success
        response.setStatus(Status.OK); // oder 200
        response.setBody(battleLog);
        return response;
    }
}
