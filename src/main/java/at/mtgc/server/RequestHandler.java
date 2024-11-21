package at.mtgc.server;

import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.util.HttpRequestParser;
import at.mtgc.server.util.HttpResponseFormatter;
import at.mtgc.server.util.HttpSocket;
import java.net.Socket;


import java.io.IOException;

public class RequestHandler {

    private final HttpSocket httpSocket;
    private final Application application;

    public RequestHandler(Socket socket, Application application) throws IOException {
        this.httpSocket = new HttpSocket(socket);
        this.application = application;
    }

    public void handle() {
        HttpRequestParser requestParser = new HttpRequestParser();
        HttpResponseFormatter responseFormatter = new HttpResponseFormatter();

        try {
            String httpRequest = httpSocket.read();
            Request request = requestParser.parse(httpRequest);

            Response response = application.handle(request);

            String httpResponse = responseFormatter.format(response);
            httpSocket.write(httpResponse);
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
