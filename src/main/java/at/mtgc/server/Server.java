package at.mtgc.server;

import at.mtgc.server.util.HttpSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final Application application;

    public Server(Application application) {
        this.application = application;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(10001)) {
            System.out.println("Server started");
            System.out.printf("Listening on port: %d\n", serverSocket.getLocalPort());

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    HttpSocket httpSocket = new HttpSocket(clientSocket);
                    RequestHandler requestHandler = new RequestHandler(httpSocket, application);
                    requestHandler.handle();
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error starting server", e);
        }
    }
}
