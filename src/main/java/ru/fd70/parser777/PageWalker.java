package parser777;

import initial.AFuncs;
import initial.CDF;

import org.apache.commons.lang3.tuple.Pair;
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
 *      -- ЧТОЗА: Пускать в отдельные треды запросы по линкам, занимает много времени
 * */
public class PageWalker {

//    static ArrayList<PageWalker>

    boolean RUNNING = true;
    public char MARKER = '0';

    final ChromeDriver driver;
    final Logger logger;
    final String baseLink;

    private Set<Cookie> cookieSet = null;

    public void setCookies(Set<Cookie> _cs) {
        cookieSet = _cs;
    }

    // that is for: private boolean wrongStartOfRawLink (String rawLink)
    private static Set<String> linkStartingFilter = Set.of(
            "#", "viber:", "blob:", "mailto:");
    public static boolean ENDS_ANNOUNCEMENT_ENABLED = false;


    private volatile int COUNTER = 0;
    private final ArrayList<String> allLinks = new ArrayList<>();
    private final ArrayList<String> respondedLinks = new ArrayList<>();

    private synchronized ArrayList<String> getAllLinksList() {
        return this.allLinks;
    }

    private synchronized ArrayList<String> getRespondedLinksList() {
        return this.respondedLinks;
    }

    private synchronized Pair<Integer, String> getFromAllLinks(int linkNumber) throws Exception {
        if (this.allLinks.size() > linkNumber) {
            COUNTER++;
            return Pair.of(linkNumber, this.allLinks.get(linkNumber));
        } else {
            throw new Exception("NPE in getFromAllLinks!");
        }
    }

    private synchronized boolean checkAndAddNewLink(String checkThat) {
        if (checkThat.startsWith(allLinks.get(0)) &&
                !endsWithJsIcoEtc(checkThat) &&
                !allLinks.contains(checkThat)) {
            allLinks.add(checkThat);
            return true;
        }
        return false;
    }

    public PageWalker(ChromeDriver driver, Logger logger, String baseUrl) {
        this.driver = driver;
        this.logger = logger;
        this.baseLink = baseUrl;
    }

    public void run() {
        if (allLinks.size() == 0) {
            this.allLinks.add(baseLink);
        }
        mainloop();
    }

    protected void mainloop() {
        try {
            while (getAllLinksList().size() > COUNTER) {
                parseRoute(getFromAllLinks(COUNTER));
            }
        } catch (IndexOutOfBoundsException i008e) {
            logger.error(i008e.getCause() + i008e.getMessage());
            logger.error(Arrays.toString(i008e.getStackTrace()));
        } catch (Exception e) {
            logger.error("Я сломался где-то");
            logger.error(e.getCause() + " <<==>> " + e.getMessage());
            // чтобы в цикле не переписать все логи
            AFuncs.sleep(1000);
            for (StackTraceElement ste : e.getStackTrace()) {
                if (ste.toString().contains("parser777.")) {
                    logger.error(ste.toString());
                }
            }
            mainloop();
        }
    }

    private void parseRoute(Pair<Integer, String> enumeratedLink) {

        int linkNumber = enumeratedLink.getLeft();
        String nextUrl = enumeratedLink.getRight();
        logger.info("[" + linkNumber + "] " + nextUrl);
        driver.get(nextUrl);

        //FIXME это нужно убирать, но без нее не успевает прогрузить страницы
        AFuncs.sleep(4000);

        ArrayList<String> rawLinks = Parser777.returnLinksFromHTML(driver.getPageSource());
        // список, который нужен для вывода всех добавленных с *метода ссылок
        // cycleEndsAnnouncement(..)
        ArrayList<String> addedInAllLinks = new ArrayList<>();

        for (String _l : rawLinks) {
            if (wrongStartOfRawLink(_l)) {
                continue;
            }
            String itMustBeCorrectLink = completeLink(_l);

            // чтоза: здесь делать ответвление в отдельный поток, для получения кодов ответа по ссылкам
            //  Если не разделю на сущности, придется весь цикл создавать новые треды, а таких может быть очень *много*
            int responseCode;

            try {
                if (getRespondedLinksList().contains(itMustBeCorrectLink)) {
                    continue; // Уже проходил ответ по этой ссылке
                } else {

                    responseCode = getResponseCodeFromLink(itMustBeCorrectLink);
                }
            } catch (Exception e) {
                logger.error(this.MARKER + "_[RA] Произошел прикол.");
                logger.error(e.getCause() + " <<==>> " + e.getMessage() + "\n"
                        + "был запрос по: " + itMustBeCorrectLink);
                continue;  // Переходим к следующей "raw" ссылке
            }

            getRespondedLinksList().add(itMustBeCorrectLink);

            if (responseCode != 200) {
                logger.warn(itMustBeCorrectLink);
                logger.warn("response code: " + responseCode);
            } else {
                if (checkAndAddNewLink(itMustBeCorrectLink)) {
                    addedInAllLinks.add(itMustBeCorrectLink);
                }
            }
        }

        cycleEndsAnnouncement(addedInAllLinks, ENDS_ANNOUNCEMENT_ENABLED);
    }

    private boolean endsWithJsIcoEtc(String itMayBeLink) {
        // /favicon.ico -- /7ae926c.js -- ненужные окончания
        return itMayBeLink.endsWith(".js")
                || itMayBeLink.endsWith(".pdf")
                || itMayBeLink.endsWith(".ico")
                || itMayBeLink.endsWith(".rss")
                || itMayBeLink.endsWith(".xml");
    }

    private boolean wrongStartOfRawLink(String rawLink) {
        // Другие протоколы, заглушка
        for (String oneOfEx : linkStartingFilter) {
            if (rawLink.startsWith(oneOfEx)) {
                return true;
            }
        }
        return false;
    }

    private String completeLink(String shortLink) {
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
            System.out.println(this.MARKER + "_-^" + code + "^- " + link);
        } else {
            System.out.println(this.MARKER + "_-<" + code + ">-" + link);
        }
        return code;
    }

    private void cycleEndsAnnouncement(ArrayList<String> arrayList, boolean describeEach) {
        if (arrayList.size() > 0) {
            logger.info(this.MARKER + "_(" + arrayList.size() + ") ссылок было добавлено");
            if (describeEach) {
                for (String link : arrayList) {
                    logger.info(link);
                }
            }
        }
    }
}

//    @Deprecated
//    private void newPWInstance () {
//        new PageWalker_inner(CDF.initCD(), logger, allLinks.get(0)).run();
//    }
//    @Deprecated
//    private static class PageWalker_inner extends PageWalker {
//        public PageWalker_inner(ChromeDriver driver, Logger logger, String baseUrl) {
//            super(driver, logger, baseUrl);
//        }
//        @Override
//        public void run() {
//            mainloop();
//        }
//        @Override
//        protected synchronized ArrayList<String> getAllLinksList() {
//            return super.getAllLinksList();
//        }
//        @Override
//        protected synchronized ArrayList<String> getRespondedLinksList () {
//            return super.getRespondedLinksList();
//        }
//        @Override
//        protected synchronized Pair<Integer, String> getFromAllLinks(int linkNumber) throws Exception {
//            return super.getFromAllLinks(linkNumber);
//        }
//        @Override
//        protected synchronized void checkAndAddNewLink(String checkThat) {
//            super.checkAndAddNewLink(checkThat);
//        }
//}
