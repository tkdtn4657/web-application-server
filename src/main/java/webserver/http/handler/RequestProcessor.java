package webserver.http.handler;

import webserver.http.RequestData;

import java.io.DataOutputStream;
import java.net.URI;

public interface RequestProcessor {

    void processing(RequestData data);

    static RequestProcessor getProcessor(RequestMethod method){
        RequestProcessor processor = null;
        switch(method){
            case GET:
                processor = new GetRequestProcessor();
                break;
            case POST:
                processor = new PostRequestProcessor();
                break;
        }
        return processor;
    }
}
