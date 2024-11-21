package at.mtgc.server.http;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private Method method;
    private String path;
    private final Map<String, String> headers = new HashMap<>();
    private String body;
    private String http; // Neues Feld hinzugefügt

    // Getter und Setter für http
    public String getHttp() {
        return http;
    }

    public void setHttp(String http) {
        this.http = http;
    }

    // Getter und Setter für andere Felder
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
