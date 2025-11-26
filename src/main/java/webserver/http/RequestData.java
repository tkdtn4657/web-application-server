package webserver.http;

import webserver.http.handler.RequestMethod;

import java.io.DataOutputStream;
import java.net.URI;
import java.util.Map;
public record RequestData (
        DataOutputStream dos,
        URI requestURI,
        String requestBody,
        RequestMethod method,
        boolean logined,
        Map<String, String> headers){
}
