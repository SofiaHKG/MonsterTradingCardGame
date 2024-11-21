package at.mtgc.server.http;

public enum Status {
    OK(200, "OK"),
    CREATED(201, "CREATED"),
    NOT_FOUND(404, "Not Found");

    private final int code;
    private final String message;

    Status(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
