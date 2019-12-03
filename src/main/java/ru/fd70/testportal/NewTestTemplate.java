package ru.fd70.testportal;
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
public class NewTestTemplate extends PageWalker {
    private static int THIS_INSTANCE = 0;

    public NewTestTemplate() {
        PropertyConfigurator.configure(getPropertiesPath("log4j").toString());
    }

    private String getLoggerName() {
        String name = new Throwable().getStackTrace()[2].getClassName();
//        for (StackTraceElement ss:new Throwable().getStackTrace()) {
//            System.out.println(ss.toString());
//        }
        LinkedList<String> ll = new LinkedList<>(Arrays.asList(name.split("\\.")));
        return ll.getLast();
    }
    protected Logger logger = LoggerFactory.getLogger(++THIS_INSTANCE + "'" + this.getLoggerName());
    protected Properties var = getProperties("main");
    protected void print(int s) { this.logger.trace(s + ""); }
    public void run(ChromeDriver chromeDriver, int wTimeSecBeforeClose) {
        long startTime = System.currentTimeMillis();
        try {
            logger.warn("Start");
            //this.main(chromeDriver);
            this.getClass().getDeclaredMethod("main", ChromeDriver.class).invoke(this, chromeDriver);
            logger.warn("end after " + (System.currentTimeMillis() - startTime)/1000);
            sleep(wTimeSecBeforeClose, 0);
        } catch (Exception e) {
            logger.error("---> drop after " + (System.currentTimeMillis() - startTime)/1000 + "s with:");
            logger.error(e.getCause() + e.getMessage());
            for (StackTraceElement ste: e.getStackTrace()) {
                if (ste.toString().contains("ru.cfmc.")) {
                    logger.error(ste.toString());
                }
            }
            sleep(wTimeSecBeforeClose, 0);
        }
    }
    public void run(ChromeDriver chromeDriver) {
        run(chromeDriver, 0);
    }
    void main(ChromeDriver chromeDriver) throws Exception {}
}