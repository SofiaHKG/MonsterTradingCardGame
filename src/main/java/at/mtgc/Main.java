package at.mtgc;

import at.mtgc.application.html.SimpleHtmlApplication;
import at.mtgc.application.echo.EchoApplication;
import at.mtgc.server.Router;
import at.mtgc.server.Server;

public class Main {
    public static void main(String[] args) {
        Router router = new Router();
        router.addRoute("/echo", new EchoApplication());
        router.addRoute("/html", new SimpleHtmlApplication());

        Server server = new Server(router);
        server.start();
    }
}
