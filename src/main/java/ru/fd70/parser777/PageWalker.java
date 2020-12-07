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
 *      -- ЧТОЗА: Добавить возможность параллельно запускать несколько "CDriver"
 *      -- TODO: Возможность настройки: проверять ссылки с других url, или нет
 * */
public class PageWalker {

//    static ArrayList<PageWalker>

    static int COUNT_OF_THREADS = 1;
    static boolean RUNNING = true;

    final ChromeDriver driver;
    final Logger logger;
    final String baseLink;

    private static Set<Cookie> cookieSet = null;
    public static void setCookies (Set<Cookie> _cs) {
        cookieSet = _cs;
    }
    public static void setCountOfThreads(int count) {
        COUNT_OF_THREADS = count;
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

    // ссыром: нужно делать коллекцию экземпляров *ЭТОГО* класса, а не CD
    //  конкретно запуска цикла mainloop()

    private static synchronized void addInAllLinks (String link) {
        allLinks.add(link);
    }
    private static synchronized boolean allLinksHaveAnotherOne (int counterValue) {
        // WHAT A FAK тут проблемка может случиться, и тогда > counterValue + 1;
        return allLinks.size() > counterValue;
    }
    private static synchronized Pair<Integer, String> getFromAllLinks (int linkNumber) throws Exception {
        if (allLinksHaveAnotherOne(linkNumber)) {
            COUNTER++;
            return Pair.of(linkNumber, allLinks.get(linkNumber));
        } else {
            throw new Exception("NPE in getFromAllLinks!");
        }
    }
    private static synchronized boolean containsInAllLinks (String link) {
        return allLinks.contains(link);
    }
    private static synchronized void addInRespondedLinks (String link) {
        respondedLinks.add(link);
    }
    private static synchronized boolean containsInRespondedLinks (String link) {
        return respondedLinks.contains(link);
    }

    private static synchronized void checkAndAddNewLink(String checkThat, ArrayList<String> arrayList) {
        if (    checkThat.startsWith(allLinks.get(0)) &&
                !endsWithJsIcoEtc(checkThat) &&
                !allLinks.contains(checkThat)) {
            //addInAllLinks(checkThat);
            allLinks.add(checkThat);
            arrayList.add(checkThat);
        }
    }

    // чтоза: можно добавить приватный конструктор, который будет инициализировать CD изнутри и только
    public PageWalker (ChromeDriver driver, Logger logger, String baseUrl) {
        this.driver = driver;
        this.logger = logger;
        this.baseLink = baseUrl;

        allLinks.add(baseLink);
        mainloop();
    }

    // чтоза: добавил волатайл, надеюсь, отдельные методы не придется прописывать для инта
    private static volatile int COUNTER = 0;
    private void mainloop () {
        try {
            // чтоза GATE = true; GATE = false;
            //  while (GATE) { ...
            while (RUNNING) {
                // чтоза: возможно, надо будет добавить статус работы каждока экз, класса PageWalker
                // What: добавить тело цикла, внутри:
                //  проверка на количество оставшихся ссылок соответственно
                while (allLinksHaveAnotherOne(COUNTER)) {
                    parseRoute(getFromAllLinks(COUNTER));
                }
                // чтоза: поток прошел через все доступные ему ссылки
                //  Здесь нужно регулировать продолжение работы, либо завершение его nahoy
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

    private void parseRoute(Pair<Integer, String> enumeratedLink) {

//        String nextUrl = allLinks.get(linkNumber);
//        logger.info("[" + linkNumber + "] " + nextUrl);
//        driver.get(nextUrl);

        int linkNumber = enumeratedLink.getLeft();
        String nextUrl = enumeratedLink.getRight();
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
                if (containsInRespondedLinks(itMustBeCorrectLink)) {
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

            addInRespondedLinks(itMustBeCorrectLink);
            //respondedLinks.add(itMustBeCorrectLink);

            if (responseCode != 200) {
                logger.warn(itMustBeCorrectLink);
                logger.warn("response code: " + responseCode);
            } else {
                checkAndAddNewLink(itMustBeCorrectLink, addedInAllLinks);
            }
        }

        cycleEndsAnnouncement(addedInAllLinks, false);
        //return allLinks.size() > linkNumber + 1;
    }

    private static boolean endsWithJsIcoEtc (String itMayBeLink) {
        // /favicon.ico -- /7ae926c.js -- ненужные окончания
        return itMayBeLink.endsWith(".js")
                || itMayBeLink.endsWith(".pdf")
                || itMayBeLink.endsWith(".ico")
                || itMayBeLink.endsWith(".rss")
                || itMayBeLink.endsWith(".xml");
    }
    private boolean wrongStartOfRawLink (String rawLink) {
        // Другие протоколы, заглушка
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

    @Deprecated
    private void newPWInstance () {
        // WHAT: Передавать параметры родительского CD в CDF
        new PageWalker_inner(CDF.initCD(), logger, allLinks.get(1));
    }
    @Deprecated
    private class PageWalker_inner extends PageWalker {
        public PageWalker_inner(ChromeDriver driver, Logger logger, String baseUrl) {
            super(driver, logger, baseUrl);
        }
    }
}

// чтоза:
//  Сделать список всех экземпляров
//  -- основной + дочерние экземпляры
//  Чекать в отдельном потоке статус каждого экземпляра
//  Если кто-то простаивает, и еще есть ссылки, --> иди работай
//  Если все простаивают и ссылок больше нет --> вырубать все!

// ссыром: сделать статик? класс PageWalker_D(), копирующее поведение родителя
//  переписать методы для взаимодействия с родительским списком
//  @Override get/addInAllList
//  //parseLinksWhenTheyAre --> parseRoute
//  Возможность редактирования этой *штуки
