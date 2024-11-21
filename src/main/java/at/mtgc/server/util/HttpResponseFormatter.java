package at.mtgc.server.util;

import at.mtgc.server.http.Response;
import at.mtgc.server.http.Status;

import java.util.Map;

public class HttpResponseFormatter {

    public String format(Response response) {
        if (response.getStatus() == null) {
            throw new IllegalStateException("Response does not contain a status");
        }

        StringBuilder formattedResponse = new StringBuilder();
        formattedResponse.append("HTTP/1.1 ")
                .append(response.getStatus().getCode())
                .append(" ")
                .append(response.getStatus().getMessage())
                .append("\r\n");

        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            formattedResponse.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        formattedResponse.append("\r\n");

        if (response.getBody() != null) {
            formattedResponse.append(response.getBody());
        }

        return formattedResponse.toString();
    }
}
