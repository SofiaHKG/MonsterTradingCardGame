package at.mtgc;

import at.mtgc.server.Application;
import at.mtgc.server.Server;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(new Application() {
            @Override
            public Response handle(Request request) {
                Response response = new Response();
                response.setStatus(Status.OK);
                response.setHeader("Content-Type", "text/plain");
                response.setBody("Hello, World!");
                return response;
            }
        });
        server.start();
    }
}
