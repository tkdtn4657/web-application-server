package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.handler.RequestMethod;
import util.HttpRequestUtils;
import webserver.http.handler.RequestProcessor;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}",
                connection.getInetAddress(),
                connection.getPort()
        );

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder requestData = new StringBuilder();

            String line;
            String[] requestFirstLines = reader.readLine().split(" ");

            // ex : GET index.html/ HTTP/1.1
            final String requestMethodString = requestFirstLines[0];
            final URI requestUri = URI.create(requestFirstLines[1]);
            final String requestProtocol = requestFirstLines[2];
            final String pathOnly = requestUri.getPath();
            RequestMethod requestMethod = RequestMethod.valueOf(requestMethodString);

            //first Line Request
            byte[] body = null;
            String contentType = null;
            RequestProcessor prossor = RequestProcessor.getProcessor(requestMethod);

            /* 제거 예정
            switch (requestMethod){
                case GET:

                    RequestProcessor processor = RequestProcessor.getProcessor(requestMethod, dos);



                    if(requestUri.getQuery() != null){
                        final String queryString = requestUri.getQuery();
                        Map<String, String> queryStringParsedData = HttpRequestUtils.parseQueryString(queryString);

                        User newUser = new User(
                                queryStringParsedData.get("userId"),
                                queryStringParsedData.get("password"),
                                queryStringParsedData.get("name"),
                                queryStringParsedData.get("email")
                        );

                        log.info("userData : {}", newUser);
                    }
                    try {
                        File responseFile = new File("./webapp" + pathOnly);
                        body = Files.readAllBytes(responseFile.toPath());
                        contentType = contentTypeParser(responseFile.toPath());
                    } catch (NoSuchFileException e){
                        String notFoundText = "notFound";
                        body = notFoundText.getBytes();
                        response404Header(dos, body.length);
                        responseBody(dos, body);
                        return;
                    }

                    requestData.append(requestFirstLines[0]).append(" ").append(requestFirstLines[1]).append(" ").append(requestFirstLines[2]).append("\n");

                    response200Header(dos, body.length, contentType);
                    break;
                case POST :
                case PUT :
                case PATCH :
                case DELETE :
                default:
                    String notFoundText = "not Support Method";
                    body = notFoundText.getBytes();
                    response404Header(dos, body.length);
                    break;
            }

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                requestData.append(line).append("\n");
            }
*/
            responseBody(dos, body);
            log.info("RequestData\n{}", requestData);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos, int lengthOfBodyContent){
        try {
            dos.writeBytes("HTTP/1.1 404 ERROR \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static String contentTypeParser(Path path) {
        // 1) OS가 알려주면 사용
        try {
            String ct = Files.probeContentType(path);
            if (ct != null) return ct;
        } catch (IOException ignore) {}

        // 2) 확장자로 보정
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html; charset=utf-8";
        if (name.endsWith(".css"))  return "text/css; charset=utf-8";
        if (name.endsWith(".js"))   return "application/javascript; charset=utf-8";
        if (name.endsWith(".json")) return "application/json; charset=utf-8";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".svg"))  return "image/svg+xml";
        if (name.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }
}
