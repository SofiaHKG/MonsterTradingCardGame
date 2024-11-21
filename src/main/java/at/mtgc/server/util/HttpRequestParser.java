package at.mtgc.server.util;

import at.mtgc.server.http.Method;
import at.mtgc.server.http.Request;

public class HttpRequestParser {
    public Request parse(String http) {
        Request request = new Request();
        String[] lines = http.split("\r\n");
        String[] requestLine = lines[0].split(" ");

        try {
            request.setMethod(Method.valueOf(requestLine[0]));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unsupported HTTP method: " + requestLine[0]);
        }

        request.setPath(requestLine[1]);

        return request;
    }
}
