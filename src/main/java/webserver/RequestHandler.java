package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;
import webserver.http.RequestData;
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

            Map<String, String> headers = new LinkedHashMap<>();
            RequestMethod requestMethod = RequestMethod.valueOf(requestMethodString);
            requestData.append(requestFirstLines[0]).append(" ").append(requestFirstLines[1]).append(" ").append(requestFirstLines[2]).append("\n");
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                HttpRequestUtils.Pair p = HttpRequestUtils.parseHeader(line);

                if(p != null){
                    headers.put(p.getKey().toLowerCase(), p.getValue());
                }

                requestData.append(line).append("\n");
            }
            //requestBody 직전까지 파싱

            String body = "";
            String cl = headers.get("content-length");
            int contentLength = 0;

            if(cl != null){
                contentLength = Integer.parseInt(cl);
            }

            if (contentLength > 0) {
                body = IOUtils.readData(reader, contentLength);
            }

            log.info("RequestData\n{}", requestData);

            RequestData data = new RequestData(dos, requestUri, body, requestMethod);
            RequestProcessor processor = RequestProcessor.getProcessor(data.method());
            processor.processing(data);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }




}
