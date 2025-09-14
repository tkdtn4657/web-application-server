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

            RequestMethod requestMethod = RequestMethod.valueOf(requestMethodString);

            RequestProcessor processor = RequestProcessor.getProcessor(requestMethod);
            processor.processing(dos, requestUri);
            
            //입력 값 로깅
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                requestData.append(line).append("\n");
            }
            requestData.append(requestFirstLines[0]).append(" ").append(requestFirstLines[1]).append(" ").append(requestFirstLines[2]).append("\n");
            log.info("RequestData\n{}", requestData);


        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }




}
