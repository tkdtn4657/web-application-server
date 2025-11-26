import org.junit.Test;
import util.HttpRequestUtils;
import webserver.http.HttpRequest;
import webserver.http.RequestData;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest {
    private String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
        HttpRequest request = new HttpRequest(in, null);

        assertEquals("GET", request.getReqData().method().toString());
        assertEquals("/user/create", request.getReqData().requestURI().getPath());
        assertEquals("keep-alive", request.getReqData().headers().get("connection"));
        assertEquals("javajigi", HttpRequestUtils.parseQueryString(request.getReqData().requestURI().getQuery()).get("userId"));
    }

    @Test
    public void request_POST() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
        HttpRequest request = new HttpRequest(in, null);

        assertEquals("POST", request.getReqData().method().toString());
        assertEquals("/user/create", request.getReqData().requestURI().getPath());
        assertEquals("keep-alive", request.getReqData().headers().get("connection"));
        assertEquals("javajigi", HttpRequestUtils.parseQueryString(request.getReqData().requestBody()).get("userId"));
    }

}
