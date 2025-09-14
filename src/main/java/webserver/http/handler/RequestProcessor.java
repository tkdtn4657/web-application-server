package webserver.http.handler;

import java.io.DataOutputStream;
import java.net.URI;

public interface RequestProcessor {

    void processing(DataOutputStream dataOutputStream, URI requestUri);

    static RequestProcessor getProcessor(RequestMethod method){
        switch(method){
            case GET:
                return new GetRequestProcessor();
            case POST:
                return new PostRequestProcessor();
        }
        return null;
    }
}
