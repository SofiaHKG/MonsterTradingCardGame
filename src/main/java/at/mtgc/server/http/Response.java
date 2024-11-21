package at.mtgc.server.http;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private Status status;
    private final Map<String, String> headers = new HashMap<>();
    private String body;

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public void setHeader(String key, String value) { headers.put(key, value); }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
