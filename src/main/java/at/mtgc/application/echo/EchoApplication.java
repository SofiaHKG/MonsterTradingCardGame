package at.mtgc.application.echo;

import at.mtgc.server.Application;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;

public class EchoApplication implements Application {

    @Override
    public Response handle(Request request) {
        Response response = new Response();
        response.setStatus(Status.OK);
        response.setHeader("Content-Type", "text/plain");
        response.setBody("Echo: " + request.getBody());

        return response;
    }
}
