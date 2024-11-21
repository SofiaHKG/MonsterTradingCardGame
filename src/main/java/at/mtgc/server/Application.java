package at.mtgc.server;

import at.mtgc.server.http.Request;
import at.mtgc.server.http.Response;

public interface Application {
    Response handle(Request request);
}
