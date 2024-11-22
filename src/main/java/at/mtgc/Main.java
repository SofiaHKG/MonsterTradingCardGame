package at.mtgc;

import at.mtgc.application.html.SimpleHtmlApplication;
import at.mtgc.application.echo.EchoApplication;
import at.mtgc.server.Router;
import at.mtgc.server.Server;

public class Main {
    public static void main(String[] args) {
        // Router erstellen und mit Standardrouten initialisieren
        Router router = new Router();

        // Statische Routen hinzufügen
        router.addRoute("/echo", new EchoApplication());
        router.addRoute("/html", new SimpleHtmlApplication());

        // Dynamische Routen für die REST-API initialisieren
        router.initializeRoutes();

        // Server starten
        Server server = new Server(router);
        server.start();
    }
}
