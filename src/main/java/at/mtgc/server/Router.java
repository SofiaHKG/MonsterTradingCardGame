package at.mtgc.server;

import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;
import at.mtgc.application.user.controller.UserController;
import at.mtgc.application.user.service.UserService;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.application.packages.controller.PackageController;
import at.mtgc.application.packages.service.PackageService;
import at.mtgc.application.packages.repository.PackageRepository;
import at.mtgc.application.trading.controller.TradingController;
import at.mtgc.application.trading.repository.TradingRepository;
import at.mtgc.application.trading.service.TradingService;
import at.mtgc.application.battle.controller.BattleController;
import at.mtgc.application.battle.service.BattleService;

import java.util.HashMap;
import java.util.Map;

public class Router implements Application {
    private final Map<String, Application> routes = new HashMap<>();

    public Router() {
        initializeRoutes();
    }

    public void addRoute(String path, Application application) {
        routes.put(path, application);
    }

    @Override
    public Response handle(Request request) {
        System.out.println("Router received request: "
                + request.getMethod() + " " + request.getPath()); // Debug

        // Original full path (e.g. "/deck?format=plain"):
        String rawPath = request.getPath();

        // Split into pathOnly and queryString
        String pathOnly;
        String queryString = null;

        int questionMarkIndex = rawPath.indexOf('?');
        if(questionMarkIndex != -1) {
            pathOnly = rawPath.substring(0, questionMarkIndex);
            queryString = rawPath.substring(questionMarkIndex + 1);
        } else {
            // no '?', so no query string
            pathOnly = rawPath;
        }

        request.setPath(pathOnly);
        request.setQueryString(queryString);

        if(routes.containsKey(pathOnly)) {
            return routes.get(pathOnly).handle(request);
        }

        if(pathOnly.startsWith("/tradings/")) {
            return routes.get("/tradings").handle(request);
        }

        if(pathOnly.startsWith("/users/")) {
            return routes.get("/users/{username}").handle(request);
        }

        Response response = new Response();
        response.setStatus(Status.NOT_FOUND);
        response.setHeader("Content-Type", "text/plain");
        response.setBody("404 Not Found");
        return response;
    }

    public void initializeRoutes() {
        // User Routes
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        UserController userController = new UserController(userService);

        addRoute("/users", userController);
        addRoute("/users/{username}", userController);
        addRoute("/sessions", userController);
        addRoute("/cards", userController);
        addRoute("/deck", userController);
        addRoute("/stats", userController);
        addRoute("/scoreboard", userController);

        // Package Routes
        PackageRepository packageRepository = new PackageRepository();
        PackageService packageService = new PackageService(packageRepository);
        PackageController packageController = new PackageController(packageService);

        addRoute("/packages", packageController);
        addRoute("/transactions/packages", packageController);

        TradingRepository tradingRepository = new TradingRepository();
        TradingService tradingService = new TradingService(tradingRepository, userRepository);
        TradingController tradingController = new TradingController(tradingService);

        addRoute("/tradings", tradingController);

        BattleService battleService = new BattleService(userRepository);
        BattleController battleController = new BattleController(battleService);

        addRoute("/battles", battleController);
    }
}