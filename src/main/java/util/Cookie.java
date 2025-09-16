package util;

public class Cookie {
    private final boolean cookieFlag;
    private final String cookieKey;
    private final String cookieValue;

    private Cookie(boolean cookieFlag, String cookieKey, String cookieValue){
        this.cookieFlag = cookieFlag;
        this.cookieKey = cookieKey;
        this.cookieValue = cookieValue;
    }

    public String getCookieKey() {
        return cookieKey;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    public boolean isAvailableCookie(){
        return cookieFlag;
    }

    public static Cookie notAvailableCookie(){
        return new Cookie(false, null, null);
    }

    public static Cookie availableCookie(String cookieKey, String cookieValue){
        return new Cookie(true, cookieKey, cookieValue);
    }
}
