// clickInput_ByXpath   - Для блоков с выпадающим списком (нужен input - что выбрать)
// clickInput_ByXpath   - Если нет input - выбирает случайно
// clickInputString     - Для обычных строковых контейнеров
package ru.fd70.funcs;
import static ru.fd70.funcs.SeFunc.sleep;

import net.bytebuddy.utility.RandomString;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public class PageWalker {
    interface  Void_time {
        void func (int time);
    }
    private static final Logger logger = LoggerFactory.getLogger("PageWalker");

    private static boolean waitForElement(ChromeDriver cd, String xpath) {
        return waitForElement(cd, xpath, 300);
    }
    protected static boolean waitForElement(ChromeDriver cd, String xpath, int timeoutInMills) {
        List<WebElement> webElement;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutInMills) {
            webElement = cd.findElementsByXPath(xpath);
            if (webElement.size() != 0) {
                try {
                    if (webElement.get(0).isEnabled() && webElement.get(0).isDisplayed()) return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }
    protected static void waitForHide(ChromeDriver cd, String xpath, int timeoutInMills) {
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutInMills) {
            if (!waitForElement(cd, xpath)) {
                return;
            }
        }
    }

    protected static void click_ByXpath(ChromeDriver cd, String xpath) {
        Void_time try_to_click = (time) -> {
            sleep(time);
            waitForElement(cd, xpath, 10000);
            cd.findElement(By.xpath(xpath)).click();
        };
        try {
            try_to_click.func(0);
        } catch (Exception e) {
            try {
                waitForHide(cd, "//div[@id='loader' and @showincrementallycount='1']", 1000);
                try_to_click.func(200);
            } catch (Exception ee) {
                waitForHide(cd, "//div[@id='loader' and @showincrementallycount='1']", 1000);
                try_to_click.func(500);
            }
        }
    }
    protected static void clickInput_ByXpath(ChromeDriver cd, String xpath, String input) {
        if (input == null) {
            clickInput_ByXpath(cd, xpath);
            return;
        }
        WebElement jr;

        click_ByXpath(cd, xpath);
        if(waitForElement(cd, "//body/span//input")) {
            jr = cd.findElement(By.xpath("//body/span//input"));
            jr.clear();
            jr.sendKeys(input.trim());
        } else {
            click_ByXpath(cd, xpath);
        }
        String path2 = "//body/span//li[contains(.,'" + input.trim() + "')]";
        click_ByXpath(cd, path2);
        waitForHide(cd, path2, 3000);
        if (!cd.findElementByXPath(xpath).getText().contains(input)) {
            click_ByXpath(cd, xpath);
            if(waitForElement(cd, "//body/span//input")) {
                jr = cd.findElement(By.xpath("//body/span//input"));
                jr.clear();
                jr.sendKeys(input.trim());
            } else {
                click_ByXpath(cd, xpath);
            }
            click_ByXpath(cd, path2);
        }
    }
    protected static void clickInput_ByXpath(ChromeDriver cd, String xpath) {
        // Не указан input - выбирает случайное значение
        WebElement jr;

        click_ByXpath(cd, xpath);
        String input = _clickRandomByXpath_getInput(cd);
        jr = cd.findElement(By.xpath("//body/span//input"));
        jr.clear();
        jr.sendKeys(input.trim());
        // Далее выбирает случайное из выпадающего списка
        String path2 = "//body/span//li[contains(.,'" + input.trim() + "')]";
        _clickRandomByXpath_click_ByXpath(cd, path2);
        waitForHide(cd, path2, 5000);
        if (!cd.findElementByXPath(xpath).getText().contains(input)) {
            click_ByXpath(cd, xpath);
            waitForElement(cd, "//body/span//input", 5000);
            jr = cd.findElement(By.xpath("//body/span//input"));
            jr.clear();
            jr.sendKeys(input.trim());
            _clickRandomByXpath_click_ByXpath(cd, path2);
        }
    }
    private static String _clickRandomByXpath_getInput (ChromeDriver cd) {
        // Special for clickInput_ByXpath (ChromeDriver, String)
        String input = "";
        int rNumber;
        List <WebElement> listWE = cd.findElementsByXPath("//body/span//li");
        final int timeout = 15000;
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            while ((System.currentTimeMillis() - startTime < timeout) && (listWE.size() < 2)) {
                waitForElement(cd,"//body/span//li");
                listWE = cd.findElementsByXPath("//body/span//li");
//                if (listWE.size() == 1) {
//                    if (listWE.get(0).getText().contains("Невозможно загрузить результаты")) {
//                        throw new Exception("Невозможно загрузить результаты");
//                    }
//                }
            }
            rNumber = (int) (Math.random() * listWE.size());
            input = listWE.get(rNumber).getText();
            if (!input.contains("Поиск") && !input.contains("Загрузка данных")) {
                if (input.contains("-")) {
                    input = input.split("-")[0];
                }
                if (input.contains("(")) {
                    input = input.split("\\(")[0];
                }
                break;
            }
        }
        // 2/3 текста (чтобы не зацепались скобки)
        return input.substring(0, input.length() * 2/3);
    }
    private static void _clickRandomByXpath_click_ByXpath (ChromeDriver cd, String xpath) {
        // Special for clickInput_ByXpath (ChromeDriver, String)
        Void_time try_to_click = (int time) -> {
            //WebDriverWait w = new WebDriverWait(cd, 5);
            sleep(time);
            waitForElement(cd, xpath, 10000);
            int rNumber = (int) (Math.random() * cd.findElementsByXPath(xpath).size());
            WebElement focus_on = cd.findElementsByXPath(xpath).get(rNumber);
            //w.until(ExpectedConditions.elementToBeClickable(focus_on));
            focus_on.click();
        };
        try {
            try_to_click.func(0);
        } catch (Exception e) {
            try {
                waitForHide(cd, "//div[@id='loader' and @showincrementallycount='1']", 1000);
                try_to_click.func(200);
            } catch (Exception ee) {
                waitForHide(cd, "//div[@id='loader' and @showincrementallycount='1']", 1000);
                try_to_click.func(500);
            }
        }
    }
    protected static void clickInputString(ChromeDriver cd, String path, String input) {
        if (input == null) {
            input = RandomString.make();
        }
        click_ByXpath(cd, path);
        WebElement jr = cd.findElement(By.xpath(path));
        jr.clear();
        jr.sendKeys(input.trim());
    }
}