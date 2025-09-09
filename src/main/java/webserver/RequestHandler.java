package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
//            log.debug("inputStream : {} ");
            DataOutputStream dos = new DataOutputStream(out);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder requestData = new StringBuilder();

            String line;
            //first Line Request
            String[] requestFirstLines = reader.readLine().split(" ");
            final String requestMethod = requestFirstLines[0];
            final String requestUri = requestFirstLines[1];
            final String requestProtocol = requestFirstLines[2];

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                requestData.append(line).append("\n");
            }
            log.info("RequestData\n{}", requestData);

            byte[] body = Files.readAllBytes(new File("./webapp"+requestUri).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
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
}
