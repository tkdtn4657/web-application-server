package webserver.http;

import util.HttpRequestUtils;
import util.IOUtils;
import webserver.http.handler.RequestMethod;

import java.io.*;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequest {

    final RequestData reqData;

    public HttpRequest(InputStream in) throws IOException {
        this.reqData = requestDataParser(in);
    }

    public RequestData getReqData() {
        return reqData;
    }

    public static RequestData requestDataParser(InputStream in) throws IOException {
        DataOutputStream dos = new DataOutputStream(OutputStream.nullOutputStream());
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
        boolean logined = false;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if(line.contains("Cookie")){
                Map<String, String> cookies = HttpRequestUtils.parseCookies(line);
                if(cookies.getOrDefault("logined", "false").equals("true")) {
                    logined = true;
                }
            } else {
                HttpRequestUtils.Pair p = HttpRequestUtils.parseHeader(line);
                if (p != null) {
                    headers.put(p.getKey().toLowerCase(), p.getValue());
                }
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

        RequestData data = new RequestData(dos, requestUri, body, requestMethod, logined, headers);
        return data;
    }
}
