package ru.fd70.testportal;

import org.openqa.selenium.chrome.ChromeDriver;
import ru.fd70.LoggedProcess;

public class ExampleClass extends LoggedProcess {

    @Override
    public void main(ChromeDriver chromeDriver) throws Exception {
        chromeDriver.get("https://www.google.com/");
    }
}
