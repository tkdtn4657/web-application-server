package webserver.http.handler;

import java.io.DataOutputStream;

public interface RequestProcessor {

    public void processing(DataOutputStream dataOutputStream);

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
