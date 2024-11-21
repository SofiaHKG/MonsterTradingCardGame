package at.mtgc.server.util;

import at.mtgc.server.http.Response;

public class HttpResponseFormatter {
    public String format(Response response) {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ").append(response.getStatus().getCode())
                .append(" ").append(response.getStatus().getMessage())
                .append("\r\n");
        builder.append("Content-Type: text/plain\r\n");
        builder.append("\r\n");
        builder.append(response.getBody());
        return builder.toString();
    }
}
