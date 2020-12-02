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
 *      -- ЧТОЗА: Добавить возможность параллельно запускать несколько "CDriver"
 *      -- TODO: Возможность настройки: проверять ссылки с других url, или нет
 * */
public class PageWalker {

    // чтоза: очевидно, что переменная нужна для одновременного запуска неск. CD
    int COUNT_OF_THREADS = 1;

    ChromeDriver driver;
    Logger logger;
    String baseLink;

    private static Set<Cookie> cookieSet = null;
    public static void setCookies (Set<Cookie> _cs) {
        cookieSet = _cs;
    }

    // that is for: private boolean wrongStartOfRawLink (String rawLink)
    private static Set<String> linkStartingFilter = Set.of(
            "#", "viber:", "blob:", "mailto:"
    );


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
        try {
            // чтоза прикол, тогда надо будет делать условие для этого интерфейса
            //  ведь каждый из f. потоков может найти новые ссылки после того,
            //  как выйдут остальные экземпляры CD
            // чтоза GATE = true; GATE = false;
            while (parseLinksWhileTheyAre(COUNTER++)) {
                // allLinks.
                // What: добавить тело цикла, внутри:
                //  проверка на количество оставшихся ссылок соответственно
            }
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

    private boolean parseLinksWhileTheyAre(int linkNumber) throws Exception {

        String nextUrl = allLinks.get(linkNumber);
        logger.info("[" + linkNumber + "] " + nextUrl);
        driver.get(nextUrl);

        //FIXME это нужно убирать, но без нее, не успевает прогрузить страницы
        AFuncs.sleep(4000);

        ArrayList<String> rawLinks = Parser777.returnLinksFromHTML(driver.getPageSource());
        ArrayList<String> addedInAllLinks = new ArrayList<>();

        for (String _l: rawLinks) {
            if (wrongStartOfRawLink(_l)) {
                continue;
            }

            String itMustBeCorrectLink = completeLink(_l);

            int responseCode;

            try {
                if (respondedLinks.contains(itMustBeCorrectLink)) {
                    continue; // Уже проходил ответ по этой ссылке
                } else {
                     responseCode = getResponseCodeFromLink(itMustBeCorrectLink);
                }
            } catch (Exception e) {
                logger.error("[RA] Произошел прикол.");
                logger.error(e.getCause() + " <<==>> " + e.getMessage() + "\n"
                        + "был запрос по: " + itMustBeCorrectLink);
                continue;  // Переходим к следующей "raw" ссылке
            }

            respondedLinks.add(itMustBeCorrectLink);

            if (responseCode != 200) {
                logger.warn(itMustBeCorrectLink);
                logger.warn("response code: " + responseCode);
            } else {
                checkAndAddNewLink(itMustBeCorrectLink, addedInAllLinks);
            }
        }

        cycleEndsAnnouncement(addedInAllLinks, false);

        // возвращает false, когда прочекается последняя доступная ссылка из "allLinks"
        // WHAT: вынести проверку списка наружу
        //  поменять интерфейс метода на void
        return allLinks.size() > linkNumber + 1;
    }


    private boolean endsWithJsIcoEtc (String itMayBeLink) {
        // /favicon.ico -- /7ae926c.js -- ненужные окончания
        return itMayBeLink.endsWith(".js")
                || itMayBeLink.endsWith(".pdf")
                || itMayBeLink.endsWith(".ico")
                || itMayBeLink.endsWith(".rss")
                || itMayBeLink.endsWith(".xml");
    }
    /** Другие протоколы, заглушка*/
    private boolean wrongStartOfRawLink (String rawLink) {
        for (String oneOfEx: linkStartingFilter) {
            if (rawLink.startsWith(oneOfEx)) {
                return true;
            }
        }
        return false;
    }
    private String completeLink (String shortLink) {
        // (//smt.th) (/smt.th)
        // Важный момент!! Чтобы не дублировалось '//' при конкатенации
        if (shortLink.startsWith("http")) {
            return shortLink;
        }
        if (shortLink.startsWith("//") || !shortLink.startsWith("/")) {
             return shortLink.replace("//", "https://");
//            return shortLink;
        } else {
            // возможно // return driver.getCurrentUrl() + shortLink;
            return baseLink + shortLink;
        }
    }

    private int getResponseCodeFromLink(String link) {
        int code;
        // делаю запрос с cookies или без них
        if (cookieSet != null) {
            code = HttpResponse.codeViaGet(link, cookieSet);
        } else {
            code = HttpResponse.codeViaGet(link);
        }
        // Здесь просто декорированный вывод в консоль, можно исключить
        if (link.contains(baseLink)) {
            System.out.println(code + "-^._.^- " + link);
        } else {
            System.out.println(code + "-<...>-" + link);
        }
        return code;
    }

    private void checkAndAddNewLink(String checkThat, ArrayList<String> arrayList) {
        // FIXME: здесь нужно убирать глобальную зависимость
        if (    checkThat.startsWith(baseLink) &&
                !endsWithJsIcoEtc(checkThat) &&
                !allLinks.contains(checkThat)) {
            allLinks.add(checkThat);
            arrayList.add(checkThat);
        }
    }
    private void cycleEndsAnnouncement (ArrayList<String> arrayList) {
        cycleEndsAnnouncement(arrayList, false);
    }
    private void cycleEndsAnnouncement (ArrayList<String> arrayList, boolean describeEach) {
        if (arrayList.size() > 0) {
            logger.info("(" + arrayList.size() + ") ссылок было добавлено");
            if (describeEach) {
                for (String link:arrayList) {
                    logger.info(link);
                }
            }
        }
    }
}
