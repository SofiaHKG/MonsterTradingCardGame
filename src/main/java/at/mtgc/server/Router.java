package at.mtgc.server;

import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;

import java.util.HashMap;
import java.util.Map;

public class Router implements Application {
    private final Map<String, Application> routes = new HashMap<>();

    public void addRoute(String path, Application application) {
        routes.put(path, application);
    }

    @Override
    public Response handle(Request request) {
        Application application = routes.get(request.getPath());
        if (application != null) {
            return application.handle(request);
        }

        Response response = new Response();
        response.setStatus(Status.NOT_FOUND);
        response.setHeader("Content-Type", "text/plain");
        response.setBody("404 Not Found");
        return response;
    }
}
