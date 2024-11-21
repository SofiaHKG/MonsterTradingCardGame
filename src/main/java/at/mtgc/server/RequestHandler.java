package at.mtgc.server;

import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.util.HttpRequestParser;
import at.mtgc.server.util.HttpResponseFormatter;
import at.mtgc.server.util.HttpSocket;

import java.io.IOException;

public class RequestHandler {

    private final HttpSocket httpSocket;
    private final Application application;

    public RequestHandler(HttpSocket httpSocket, Application application) {
        this.httpSocket = httpSocket;
        this.application = application;
    }

    public void handle() {
        HttpRequestParser parser = new HttpRequestParser();
        HttpResponseFormatter formatter = new HttpResponseFormatter();

        try {
            String httpRequest = httpSocket.read();
            if (httpRequest == null || httpRequest.trim().isEmpty()) {
                System.out.println("Received an empty request. Ignoring.");
                return;
            }

            Request request = parser.parse(httpRequest);
            Response response = application.handle(request);

            String httpResponse = formatter.format(response);
            httpSocket.write(httpResponse);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid HTTP Request: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
        } finally {
            try {
                httpSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
