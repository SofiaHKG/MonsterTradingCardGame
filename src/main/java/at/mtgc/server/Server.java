package at.mtgc.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final Application application;
    private ServerSocket serverSocket;

    public Server(Application application) {
        this.application = application;
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(10001);
            System.out.println("Server started");
            System.out.println("Listening on port: " + serverSocket.getLocalPort());
        } catch (IOException e) {
            throw new RuntimeException("Error starting the server", e);
        }

        while (true) {
            try {
                Socket socket = this.serverSocket.accept();
                RequestHandler requestHandler = new RequestHandler(socket, this.application);
                requestHandler.handle();
            } catch (IOException e) {
                System.err.println("Error handling client connection: " + e.getMessage());
            }
        }
    }
}
