package at.mtgc.server.util;

import java.io.*;
import java.net.Socket;

public class HttpSocket implements Closeable {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public HttpSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public String read() throws IOException {
        StringBuilder requestBuilder = new StringBuilder();
        String line;

        // Lese die Header
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            requestBuilder.append(line).append("\r\n");
        }

        // Prüfe, ob überhaupt etwas gelesen wurde
        if (requestBuilder.length() == 0) {
            return null;
        }

        // Lese den Body, falls Content-Length vorhanden ist
        String contentLengthHeader = requestBuilder.toString().toLowerCase();
        int contentLengthIndex = contentLengthHeader.indexOf("content-length:");
        if (contentLengthIndex != -1) {
            int contentLength = Integer.parseInt(contentLengthHeader
                    .substring(contentLengthIndex + 15).trim().split("\r\n")[0]);
            char[] body = new char[contentLength];
            reader.read(body, 0, contentLength);
            requestBuilder.append("\r\n").append(new String(body));
        }

        return requestBuilder.toString();
    }

    public void write(String http) throws IOException {
        writer.write(http);
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }
}
