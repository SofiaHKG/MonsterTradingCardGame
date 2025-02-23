package at.mtgc.server.http;

public class HttpException extends RuntimeException {
    private final Status status;

    public HttpException(Status status, String message) {
        super(message);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
