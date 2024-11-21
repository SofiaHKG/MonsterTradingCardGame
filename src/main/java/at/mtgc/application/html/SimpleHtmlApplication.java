package at.mtgc.application.html;

import at.mtgc.server.Application;
import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;

public class SimpleHtmlApplication implements Application {

    @Override
    public Response handle(Request request) {
        Response response = new Response();
        response.setStatus(Status.OK);
        response.setHeader("Content-Type", "text/html");
        response.setBody("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Hello World</title>
                </head>
                <body>
                    <h1>Hello, World!</h1>
                    <p>Path: %s</p>
                </body>
                </html>
                """.formatted(request.getPath()));

        return response;
    }
}
