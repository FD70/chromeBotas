package ru.fd70;

import ru.fd70.funcs.PageWalker;
import static ru.fd70.funcs.SeFunc.*;
import static ru.fd70.funcs.SeFunc.getProperties;

import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;

abstract public class LoggedProcess extends PageWalker implements Runnable {
    private static volatile int THIS_INSTANCE = 0;
    private int wTimeSecBeforeClose = 0;
    private ChromeDriver chromeDriver = null;

    public LoggedProcess() {
        PropertyConfigurator.configure(getPropertiesPath("log4j").toString());
    }


    private String getLoggerName() {
        LinkedList<String> name = new LinkedList<>(Arrays.asList(this.getClass().getName().split("\\.")));
        return name.getLast();
    }

    protected void printtrace(Object obj) {this.logger.trace(obj.toString());}
    protected Logger logger = LoggerFactory.getLogger(getInstanceNumber() + "'" + this.getLoggerName());

    protected Properties var = getProperties("main");

    private static synchronized int getInstanceNumber() {
        return ++THIS_INSTANCE;
    }
    public void setwTimeSecBeforeClose(int t) {
        this.wTimeSecBeforeClose = t;
    }
    public void setChromeDriver(ChromeDriver cd) {
        this.chromeDriver = cd;
    }

    private static void quitWebDriverAfter(ChromeDriver cd, int sec) {
        if (cd != null) {
            sleep(sec, 0);
            cd.quit();
        }
    }

    public void run() {

        long startTime = System.currentTimeMillis();
        try {

            logger.warn("Start");
            this.main(chromeDriver);
            logger.warn("end after " + (System.currentTimeMillis() - startTime)/1000);

            quitWebDriverAfter(chromeDriver, wTimeSecBeforeClose);
        } catch (Exception e) {
            logger.error("---> drop after " + (System.currentTimeMillis() - startTime)/1000 + "s with:");
            logger.error(e.getCause() + e.getMessage());
            for (StackTraceElement ste: e.getStackTrace()) {
                if (ste.toString().contains("ru.fd70.")) {
                    logger.error(ste.toString());
                }
            }
            quitWebDriverAfter(chromeDriver, wTimeSecBeforeClose);
        }
    }
    abstract public void main(ChromeDriver chromeDriver) throws Exception;
}
