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

    public void initializeRoutes() {
        // User Routes
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        UserController userController = new UserController(userService);

        addRoute("/users", userController);
        addRoute("/sessions", userController);

        // Package Routes
        PackageRepository packageRepository = new PackageRepository();
        PackageService packageService = new PackageService(packageRepository);
        PackageController packageController = new PackageController(packageService);

        addRoute("/packages", packageController);
    }
}
