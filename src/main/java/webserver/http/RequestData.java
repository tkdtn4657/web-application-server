package webserver.http;

import webserver.http.handler.RequestMethod;

import java.io.DataOutputStream;
import java.net.URI;

public record RequestData (DataOutputStream dos, URI requestURI, String body, RequestMethod method){
}
