package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;
import webserver.http.HttpRequest;
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
            RequestData requestData = HttpRequest.requestDataParser(in, out);

            RequestProcessor
                    .getProcessor(requestData.method())
                    .processing(requestData);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }




}
