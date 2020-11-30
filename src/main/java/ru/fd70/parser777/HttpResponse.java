package parser777;

import io.restassured.RestAssured;
import io.restassured.http.Cookies;
import org.openqa.selenium.Cookie;

import java.util.ArrayList;
import java.util.Set;


public class HttpResponse {
    public static int codeViaGet(String url) {
        return RestAssured.get(url).statusCode();
    }
    
    public static int codeViaGet(String url, Set<org.openqa.selenium.Cookie> seleniumCookies) {

        //адаптер, возможно нужно вынести из метода, чтобы каждый раз не буилдился
        ArrayList<io.restassured.http.Cookie> restAssuredCookies = new ArrayList<>();

        for (org.openqa.selenium.Cookie _cookie: seleniumCookies) {
            restAssuredCookies.add(new io.restassured.http.Cookie.Builder(_cookie.toString()).build());
        }

        return RestAssured.given()
                .cookies(new Cookies(restAssuredCookies))
                .get(url).statusCode();
    }
    
    public static int codeViaPost(String url) {
        return RestAssured.post(url).statusCode();
    }
}
