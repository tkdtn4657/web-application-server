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
import java.nio.file.Path;
import java.util.Map;

import static util.HttpRequestUtils.*;

class GetRequestProcessor implements RequestProcessor{

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    @Override
    public void processing(RequestData data) {

        final URI uri = data.requestURI();
        final String pathOnly = uri.getPath();
        final DataOutputStream dos = data.dos();

        if(data.requestURI().getQuery() != null){
            final String queryString = data.requestURI().getQuery();
            Map<String, String> queryStringParsedData = HttpRequestUtils.parseQueryString(queryString);

            User newUser = new User(
                    queryStringParsedData.get("userId"),
                    queryStringParsedData.get("password"),
                    queryStringParsedData.get("name"),
                    queryStringParsedData.get("email")
            );

            log.info("userData : {}", newUser);
        }

        byte[] body = null;
        String contentType = null;
        try {
            File responseFile = new File("./webapp" + pathOnly);
            body = Files.readAllBytes(responseFile.toPath());
            contentType = contentTypeParser(responseFile.toPath());
        } catch (IOException e) {
            String notFoundText = "notFound";
            body = notFoundText.getBytes();
            response404Header(dos, body.length);
            responseBody(dos, body);
            return;
        }
        response200Header(dos, body.length, contentType);
        responseBody(dos, body);
    }

    private static String contentTypeParser(Path path) {
        try {
            String ct = Files.probeContentType(path);
            if (ct != null) return ct;
        } catch (IOException ignore) {}

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
