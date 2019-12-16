//
package ru.fd70;

import static ru.fd70.funcs.SeFunc.getProperties;
import static ru.fd70.funcs.SeFunc.getPropertiesPath;
import static ru.fd70.funcs.SeFunc.sleep;

import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fd70.testportal.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class CDFactory {
    private static Properties var = getProperties("main");
    private static final Logger logger = LoggerFactory.getLogger("CDFactory");
    private static String getChromeDriver_path() {
        Path p0 = Paths.get("chromedriverPath_var");
        if (var.getProperty("chromedriverPath") != null) {
            p0 = Paths.get(var.getProperty("chromedriverPath"));
        }
        Path p1 = Paths.get("").toAbsolutePath().resolve("chromedriver");
        Path p2 = Paths.get("").toAbsolutePath().resolve("target/classes/chromedriver.exe");
        Path filePath;
        if (p0.toFile().exists()) {
            filePath = p0;
        } else if (p1.toFile().exists()) {
            filePath = p1;
        } else if (p2.toFile().exists()) {
            filePath = p2;
        } else {
            logger.error("Chrome driver not located here:\n" +
                    p0.toString() + "\n" +
                    p1.toString() + "\n" +
                    p2.toString());
            filePath = Paths.get("--");
        }
        //logger.debug("Chromedriver path: " + filePath);
        return filePath.toString();
    }

    private static ChromeDriver initCD() {
        System.setProperty("webdriver.chrome.driver", getChromeDriver_path());
        System.setProperty("webdriver.chrome.silentOutput", "true");
        // ChromeOptions Для запуска без окна браузера
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("headless");
        chromeOptions.addArguments("--disable-extensions");
        chromeOptions.addArguments("--no-sandbox");
        return new ChromeDriver(chromeOptions);
    }
    private static ChromeDriver initCD_windomed() {
        System.setProperty("webdriver.chrome.driver", getChromeDriver_path());
        return new ChromeDriver();
    }

    private static volatile LoggedProcess loggedProcess = null;
    private static void initProcess(ChromeDriver chromeDriver, LoggedProcess target) {
        initProcess(chromeDriver, target, 0);
    }
    private static void initProcess(ChromeDriver chromeDriver, LoggedProcess target, int wTimeSecBeforeClose) {
        target.setwTimeSecBeforeClose(wTimeSecBeforeClose);
        target.setChromeDriver(chromeDriver);
        loggedProcess = target;
    }

    public static LoggedProcess main(int sw) {
        PropertyConfigurator.configure(getPropertiesPath("log4j").toString());
        try {
            switch (sw) {
                case 42: initProcess(initCD_windomed(), new ExampleClass(), 60); break;

                default:
                    logger.trace("no any case " + sw);
            }

        } catch (Exception e) {
            logger.error("<--- --- --->");
            logger.error(e.getMessage());
            for (StackTraceElement ste: e.getStackTrace()) {
                if (ste.toString().contains("ru.fd70.")) {
                    logger.error(ste.toString());
                }
            }
        }

        return loggedProcess;
    }
}