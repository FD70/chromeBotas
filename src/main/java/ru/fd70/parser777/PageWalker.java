package parser777;

import initial.AFuncs;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;

import java.util.ArrayList;


public class PageWalker {

    ChromeDriver driver;
    Logger logger;

    String baseLink;

    private static Cookie _token = null;
    private static Cookie _refresh_token = null;

    public static void set_token (Cookie token) {
        _token = token;
    }
    public static void set_refresh_token (Cookie refresh_token) {
        _refresh_token = refresh_token;
    }


    private final ArrayList<String> allLinks = new ArrayList<>();
    private final ArrayList<String> responsedLinks = new ArrayList<>();

    public PageWalker (ChromeDriver driver, Logger logger, String baseUrl) {
        this.driver = driver;
        this.logger = logger;
        this.baseLink = baseUrl;

//        _token = driver.manage().getCookieNamed("token");
//        _refresh_token = driver.manage().getCookieNamed("refresh_token");

        allLinks.add(baseLink);
        mainloop();
    }

    private boolean endsWithJsIcoEtc (String itMayBeLink) {
        // /favicon.ico -- /7ae926c.js -- ненужные окончания
        return itMayBeLink.endsWith(".js")
                || itMayBeLink.endsWith(".pdf")
                || itMayBeLink.endsWith(".ico");
    }

    private int COUNTER = 0;
    private void mainloop () {
        try {
            while (pageWalker(COUNTER++));
        } catch (Exception e) {
            logger.error("Я сломался где-то");
            logger.error(e.getCause() + e.getMessage());
            for (StackTraceElement ste: e.getStackTrace()) {
                if (ste.toString().contains("parser777.")) {
                    logger.error(ste.toString());
                }
            }
            mainloop();
        }

    }

    private boolean pageWalker (int linkNumber) {
        
        String nextUrl = allLinks.get(linkNumber);
        logger.info("[" + linkNumber + "] " + nextUrl);
        driver.get(nextUrl);

        //FIXME это нужно убирать, но без нее, не успевает прогрузить страницы
        AFuncs.sleep(4000);

        // parse and response
        // *нужно разделить сущности
        String currentUrl = driver.getCurrentUrl();
        ArrayList<String> rawLinks = Parser777.returnLinksFromHTML(driver.getPageSource());

        for (String _l: rawLinks) {
            // дописываю ссылку
            if (!_l.startsWith("http")) {
                _l = baseLink + _l;
                // Важный момент!! Чтобы не дублировалось '//' при конкатенации
            }

            int responseCode;

            if (responsedLinks.contains(_l)) {
                // Уже проходил ответ по этой ссылке
                continue;
            } else {
                if (_l.contains(baseLink)) {
                    // вход с токеном или без
                    // здесь нужно переделать сам сеттер печенек в классе
                    // затем проверка в принципе не нужна будет
                    if (_token != null && _refresh_token != null) {
                        responseCode = HttpResponse.codeViaGet(_l, _token, _refresh_token);
                    } else {
                        responseCode = HttpResponse.codeViaGet(_l);
                    }
                    System.out.println(responseCode + "-^._.^- " + _l);
                } else {
                    responseCode = HttpResponse.codeViaGet(_l);
                    System.out.println(responseCode + "-<...>-" + _l);
                }
            }

            responsedLinks.add(_l);

            // checkResponseCodes()
            if (responseCode != 200) {
                //TODO создать словарь с ответами не 200
                //  Но, все можно увидеть в логе, в принципе
                logger.warn(currentUrl);
                logger.warn("response code: " + responseCode);
            } else {
                // addNewLink()
                if (_l.contains(baseLink)) {
                    if (!endsWithJsIcoEtc(_l)) {
                        if (!allLinks.contains(_l)) {
                            allLinks.add(_l);
                            System.out.println(" ++ + " + _l);
                        }
                    }
                }
            }
        }

        // возвращает false, когда прочекается последняя доступная ссылка из "allLinks"
        return allLinks.size() > linkNumber + 1;
    }
}
