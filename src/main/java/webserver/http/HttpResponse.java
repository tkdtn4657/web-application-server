package webserver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Cookie;
import webserver.RequestHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpResponse {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    DataOutputStream outputStream;

    String statusCode = "";
    String responseMessage = "";
    byte[] body;
    StringBuffer headers = new StringBuffer();

    public HttpResponse(OutputStream outputStream){
        this.outputStream = new DataOutputStream(outputStream);
    }

    public HttpResponse(DataOutputStream outputStream){
        this.outputStream = outputStream;
    }

    //직접 파일을 반환하는 메서드 이동
    public void forward(String path){
        addHeader("Content-Type", contentTypeParser(Path.of(path)) + ";charset=utf-8");
        addHeader("Content-Length", String.valueOf(path.length()));
        headers.append("\r\n");
        responseBody(path.getBytes());
        response();
    }

    //redirect이기 때문에 301이거나 302로 반환해야 하며
    //302일 때는 그냥 이동, 301일 때는 쿠키에 redirect flag를 담아서 반환해야함
    public void sendRedirect(String path){
        setResponseStatusCode("301");
        addHeader("Location", path);
        response();
    }

    
    public void addHeader(String key, String value){
        headers.append(key + ": " + value + "\r\n");
    }

    public static void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void responseBody(byte[] body) {
        this.body = body;
    }

    private void setResponseStatusCode(String statusCode){
        switch(statusCode){
            case "200":
                this.statusCode = "200";
                responseMessage = "OK";
                break;
            case "301":
                this.statusCode = "301";
                responseMessage = "Moved Permanently";
                break;
            default:
                this.statusCode = "404";
                responseMessage = "Not Found";
        }
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

    private void response(){
        try{
            this.outputStream.write(("HTTP/1.1 " + statusCode + " " + responseMessage + " \r\n").getBytes());
            this.outputStream.write(headers.toString().getBytes());
            if(body != null)  this.outputStream.write(body);
            this.outputStream.flush();

        } catch(IOException e){
            log.error(e.getMessage());
            //404 반환
        }
    }

    public static void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static void response302Header(DataOutputStream dos, int lengthOfBodyContent, String contentType, String location, Cookie cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            if(cookie.isAvailableCookie()){
                dos.writeBytes("Set-Cookie: " + cookie.getCookieKey() + "=" + cookie.getCookieValue() + "; Path=/;\r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static void response404Header(DataOutputStream dos, int lengthOfBodyContent){
        try {
            dos.writeBytes("HTTP/1.1 404 ERROR \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
