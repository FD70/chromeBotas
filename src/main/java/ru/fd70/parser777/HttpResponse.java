package parser777;

import initial.AFuncs;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.openqa.selenium.Cookie;


public class HttpResponse {
    public static int codeViaGet(String url) {
        return RestAssured.get(url).statusCode();
    }

    public static int codeViaGet(String url, Cookie token, Cookie ref_token) {
        return RestAssured.given()
                .cookie(token.toString())
                .cookie(ref_token.toString())
                .get(url).statusCode();
    }

    public static String viaGetReturnHtmlFile(String url, Cookie token, Cookie ref_token){
        return viaGetReturnTypeFile(url, ContentType.HTML, token, ref_token);
    }

    private static String viaGetReturnTypeFile(String url, ContentType _type, Cookie token, Cookie ref_token){
        return RestAssured.given()
                .cookie(token.toString())
                .cookie(ref_token.toString())
                .get(url).then().contentType(_type).extract().asString();
    }

    public static int codeViaPost(String url) {
        return RestAssured.post(url).statusCode();
    }

    public static void main(String[] args) {
        String s = RestAssured
                .get("http://yandex.ru/").getBody().prettyPrint();

//        for (String hhh:Parser777.returnLinksFromHTML(s)) {
//            System.out.println(hhh);
//        }
        System.out.println(s);
        System.out.println(AFuncs._l);
        System.out.println(Parser777.returnLinksFromHTML(s));
    }
}
