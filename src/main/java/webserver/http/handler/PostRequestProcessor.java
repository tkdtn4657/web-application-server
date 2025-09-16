package webserver.http.handler;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import webserver.RequestHandler;
import webserver.http.RequestData;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;

import static util.HttpRequestUtils.*;
import static util.HttpRequestUtils.responseBody;

public class PostRequestProcessor implements RequestProcessor{

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    @Override
    public void processing(RequestData data) {
        final URI uri = data.requestURI();
        final String pathOnly = uri.getPath();
        final DataOutputStream dos = data.dos();
        final String requestBody = data.requestBody();

        if(!requestBody.isEmpty()){
            Map<String, String> queryStringParsedData = HttpRequestUtils.parseQueryString(requestBody);

            User newUser = new User(
                    queryStringParsedData.get("userId"),
                    queryStringParsedData.get("password"),
                    queryStringParsedData.get("name"),
                    queryStringParsedData.get("email")
            );

            log.info("userData : {}", newUser);
        }

        byte[] body = "success".getBytes();
        String contentType = "text/html";
        response302Header(dos, body.length, contentType);
        responseBody(dos, body);
    }
}
