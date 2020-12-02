package parser777;

import initial.AFuncs;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/**
 *      Парсит ссылки, проверяет код ответа, делает все сама
 *      Делает все это на базе Selenium (chromeDriver)
 *
 *      -- чтоза Добавить возможность параллельно запускать несколько "CDriver"
 *      -- Возможность настройки: проверять ссылки с других url, или нет
 * */
public class PageWalker {

    ChromeDriver driver;
    Logger logger;
    String baseLink;

    private static Set<Cookie> cookieSet = null;
    public static void setCookies (Set<Cookie> _cs) {
        cookieSet = _cs;
    }



    private static final ArrayList<String> allLinks = new ArrayList<>();
    private static final ArrayList<String> respondedLinks = new ArrayList<>();

    // чтоза также добавить методы для вызова новых экземпляров CD,
    //  Нужно придумать еще, при каких условиях запускать остальные экземпляры CD
    //  Без запуска нескольких экземпляров, synchronized часть кода не будет иметь смысла

    private static synchronized void addInAllLinks (String link) {
        allLinks.add(link);
    }
    private static synchronized String getFromAllLinks (int linkNumber) {
        return allLinks.get(linkNumber);
        // ЧТОЗА здесь нужно увеличивать значение счетчика COUNTER;
    }

    private static synchronized boolean containsInAllLinks (String link) {
        // чтоза пустота
        return false;
    }
    private static synchronized void addInRespondedLinks (String link) {
        respondedLinks.add(link);
    }
    private static synchronized boolean containsInRespondedLinks (String link) {
        // чтоза пустота
        return false;
    }




    public PageWalker (ChromeDriver driver, Logger logger, String baseUrl) {
        this.driver = driver;
        this.logger = logger;
        this.baseLink = baseUrl;

        allLinks.add(baseLink);
        mainloop();
    }

    private int COUNTER = 0;
    private void mainloop () {
        // WHAT the: тут нужно либо вызывать метод с параметром, либо одно из двух;
        //  Хотя, как ты организуешь обход всех ссылок, не используя цикла.
        //  Значит нужно перехватывать Исключения на более низком уровне, чем здесь
        try {
            while (pageWalker(COUNTER++));
        } catch (IndexOutOfBoundsException i008e) {
            logger.error(i008e.getCause() + i008e.getMessage());
            logger.error(Arrays.toString(i008e.getStackTrace()));
        } catch (Exception e) {
            logger.error("Я сломался где-то");
            logger.error(e.getCause() + " <<==>> " + e.getMessage());
            // чтобы в цикле не переписать все логи
            AFuncs.sleep(2000);
            for (StackTraceElement ste: e.getStackTrace()) {
                if (ste.toString().contains("parser777.")) {
                    logger.error(ste.toString());
                }
            }
            mainloop();
        }
    }

    // WHAT: поменять название метода, на linkCrawler или что-то более говорящее и относящееся к сущности
    private boolean pageWalker (int linkNumber) throws Exception {

        String nextUrl = allLinks.get(linkNumber);
        logger.info("[" + linkNumber + "] " + nextUrl);
        driver.get(nextUrl);

        //FIXME это нужно убирать, но без нее, не успевает прогрузить страницы
        AFuncs.sleep(4000);

        // parse and response
        // *нужно разделить сущности
        // String currentUrl = driver.getCurrentUrl();

        ArrayList<String> rawLinks = Parser777.returnLinksFromHTML(driver.getPageSource());

        // выделить в метод(ы) ?
        for (String _l: rawLinks) {
            // дописываю ссылку, если нужно // (#) !!
            if (_l.startsWith("#")) {
                continue;
            }
            if (!_l.startsWith("http")) {
                _l = completeLink(_l);
            }


            int responseCode;

            try {
                if (respondedLinks.contains(_l)) {
                    // Уже проходил ответ по этой ссылке
                    continue;
                } else {
                    // делаю запрос с cookies или без них
                    if (cookieSet != null) {
                        responseCode = HttpResponse.codeViaGet(_l, cookieSet);
                    } else {
                        responseCode = HttpResponse.codeViaGet(_l);
                    }
                    // Здесь просто декорированный вывод в консоль, можно исключить
                    if (_l.contains(baseLink)) {
                        System.out.println(responseCode + "-^._.^- " + _l);
                    } else {
                        System.out.println(responseCode + "-<...>-" + _l);
                    }
                }
            } catch (Exception e) {
                // What: Можно не пробрасывать ошибку здесь, а просто вызывать continue; ?
                //  Скорее всего, так оно вернее будет
                throw new Exception(e.getMessage()
                        + "\n"
                        + "был запрос по: " + _l);
            }

            respondedLinks.add(_l);

            // checkResponseCodes()
            if (responseCode != 200) {
                logger.warn(_l);
                logger.warn("response code: " + responseCode);
            } else {
                // addNewLink()
                if (    _l.contains(baseLink) &&
                        !endsWithJsIcoEtc(_l) &&
                        !allLinks.contains(_l)) {
                    allLinks.add(_l);
                    System.out.println(" ++ + " + _l);
                }
            }
        }

        // возвращает false, когда прочекается последняя доступная ссылка из "allLinks"
        return allLinks.size() > linkNumber + 1;
    }

    private boolean endsWithJsIcoEtc (String itMayBeLink) {
        // /favicon.ico -- /7ae926c.js -- ненужные окончания
        return itMayBeLink.endsWith(".js")
                || itMayBeLink.endsWith(".pdf")
                || itMayBeLink.endsWith(".ico")
                || itMayBeLink.endsWith(".rss");
    }

    private String completeLink (String shortLink) {
        // (//smt.th)
        // (/smt.th)
        // Важный момент!! Чтобы не дублировалось '//' при конкатенации
        if (shortLink.startsWith("//") || !shortLink.startsWith("/")) {
             return shortLink.replace("//", "https://");
//            return shortLink;
        } else {
            // возможно // return driver.getCurrentUrl() + shortLink;
            return baseLink + shortLink;
        }
    }
}
