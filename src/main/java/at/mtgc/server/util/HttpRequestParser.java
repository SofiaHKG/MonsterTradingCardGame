package at.mtgc.server.util;

import at.mtgc.server.http.Method;
import at.mtgc.server.http.Request;

public class HttpRequestParser {
    public Request parse(String http) {
        if (http == null || http.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty HTTP request.");
        }

        Request request = new Request();
        request.setHttp(http);

        String[] lines = http.split("\r\n");
        if (lines.length < 1) {
            throw new IllegalArgumentException("Invalid HTTP request format.");
        }

        // Request Line Parsing
        String requestLine = lines[0];
        String[] requestLineParts = requestLine.split(" ");
        if (requestLineParts.length < 3) {
            throw new IllegalArgumentException("Invalid HTTP request line: " + requestLine);
        }

        request.setMethod(Method.valueOf(requestLineParts[0]));
        request.setPath(requestLineParts[1]);

        // Headers Parsing
        int emptyLineIndex = -1;
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                emptyLineIndex = i;
                break;
            }
            String[] headerParts = lines[i].split(":", 2);
            if (headerParts.length == 2) {
                request.setHeader(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        // Body Parsing
        if (emptyLineIndex != -1 && emptyLineIndex < lines.length - 1) {
            StringBuilder bodyBuilder = new StringBuilder();
            for (int i = emptyLineIndex + 1; i < lines.length; i++) {
                bodyBuilder.append(lines[i]).append("\r\n");
            }
            request.setBody(bodyBuilder.toString().trim());
        } else {
            request.setBody("");
        }

        return request;
    }
}
