package webserver.http.handler;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Cookie;
import util.HttpRequestUtils;
import webserver.RequestHandler;
import webserver.http.HttpResponse;
import webserver.http.RequestData;

import java.io.DataOutputStream;
import java.net.URI;
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
        final HttpResponse httpResponse = new HttpResponse(dos);

        Map<String, String> queryStringParsedData = null;
        if(!requestBody.isEmpty()){
            queryStringParsedData = HttpRequestUtils.parseQueryString(requestBody);
        }

        String contentType = "text/html";
        switch (uri.toString()){
            case "/user/create" :
                User newUser = new User(
                        queryStringParsedData.get("userId"),
                        queryStringParsedData.get("password"),
                        queryStringParsedData.get("name"),
                        queryStringParsedData.get("email")
                );

                log.info("userData : {}", newUser);
                try{
                    DataBase.addUser(newUser);
                } catch(Exception e) {
                    log.error("user add error");
                }

                HttpResponse.response302Header(dos, "success".getBytes().length, contentType, "/index.html", Cookie.notAvailableCookie());
                break;
            case "/user/login" :
                String userId = queryStringParsedData.get("userId");
                String userInputPassword = queryStringParsedData.get("password");

                User findUser = DataBase.findUserById(userId);

                if(findUser == null){
                    log.error("user find error");
                    HttpResponse.response404Header(dos, "success".getBytes().length);
                    return;
                }
                if(findUser.getPassword().equals(userInputPassword)){
                    byte[] body = "loginSuccess".getBytes();
                    HttpResponse.response302Header(dos, "success".getBytes().length, contentType, "/index.html", Cookie.availableCookie("logined", "true"));
                } else {
                    byte[] body = "loginFail".getBytes();
                    HttpResponse.response302Header(dos, "success".getBytes().length, contentType, "/user/login_failed.html", Cookie.availableCookie("logined", "false"));
                }
                break;
            default:
                break;
        }

        byte[] body = "success".getBytes();
        HttpResponse.responseBody(dos, body);
    }
}
