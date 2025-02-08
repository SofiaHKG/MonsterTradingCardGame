package at.mtgc.server;

import at.mtgc.server.util.HttpSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final Application application;
    // Creating a thread pool with 10 threads
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public Server(Application application) {
        this.application = application;
    }

    public void start() {
        try(ServerSocket serverSocket = new ServerSocket(10001)) {
            System.out.println("Server started. Listening on port: " + serverSocket.getLocalPort());
            while(true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // Transfering client connection to the thread pool
                    executorService.submit(() -> {
                        try {
                            HttpSocket httpSocket = new HttpSocket(clientSocket);
                            RequestHandler requestHandler = new RequestHandler(httpSocket, application);
                            requestHandler.handle();
                        } catch(IOException e) {
                            System.err.println("Error processing client connection: " + e.getMessage());
                        }
                    });
                } catch(IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch(IOException e) {
            throw new RuntimeException("Error starting the server", e);
        } finally {
            // Clean shutdown of the executors
            executorService.shutdown();
        }
    }
}
