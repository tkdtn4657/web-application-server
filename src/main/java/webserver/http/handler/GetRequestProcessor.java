package webserver.http.handler;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Cookie;
import webserver.RequestHandler;
import webserver.http.RequestData;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static util.HttpRequestUtils.*;

class GetRequestProcessor implements RequestProcessor{

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    @Override
    public void processing(RequestData data) {

        final URI uri = data.requestURI();
        final String pathOnly = uri.getPath();
        final DataOutputStream dos = data.dos();

        byte[] body = null;
        String contentType = null;
        switch (uri.toString()){
            case "/user/list" :
                if(data.logined()){
                    Collection<User> users = DataBase.findAll();
                    StringBuilder sb = new StringBuilder();
                    sb.append("<table border='1'>");
                    for (User user : users) {
                        sb.append("<tr>");
                        sb.append("<td>" + user.getUserId() + "</td>");
                        sb.append("<td>" + user.getName() + "</td>");
                        sb.append("<td>" + user.getEmail() + "</td>");
                        sb.append("</tr>");
                    }
                    sb.append("</table>");
                    body = sb.toString().getBytes();
                    response200Header(dos, body.length, contentType);
                } else {
                    response302Header(dos, "success".getBytes().length, contentType, "/login.html", Cookie.availableCookie("logined", "false"));
                }
                break;
            default:
                try {
                    File responseFile = new File("./webapp" + pathOnly);
                    body = Files.readAllBytes(responseFile.toPath());
                    contentType = contentTypeParser(responseFile.toPath());
                    response200Header(dos, body.length, contentType);
                } catch (IOException e) {
                    String notFoundText = "notFound";
                    body = notFoundText.getBytes();
                    response404Header(dos, body.length);
                    responseBody(dos, body);
                    return;
                }
        }
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
