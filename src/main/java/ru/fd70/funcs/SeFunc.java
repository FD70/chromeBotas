//
package ru.fd70.funcs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SeFunc {
    private static final Logger logger = LoggerFactory.getLogger("SeFunc");

    public static void sleep(int millis) {
        sleep(0, millis);
    }
    public static void sleep(int sec, int mills) {
        try {
            int seconds = sec * 1000;
            Thread.sleep(seconds + mills);
        } catch (InterruptedException e) {
            logger.error("\n\n<SeFunc sleep Exception>");
            for (StackTraceElement ste: e.getStackTrace()) logger.error(ste.toString());
        }
    }

    public static Path getPropertiesPath(String cName) {
        // Try to find cName.properties
        Path p0 = Paths.get("").toAbsolutePath().resolve("TestPortal/" + cName + ".properties");
        Path p1 = Paths.get("").toAbsolutePath().resolve("properties/" + cName + ".properties");
        Path p2 = Paths.get("").toAbsolutePath().resolve(cName + ".properties");
        Path p3 = Paths.get("").toAbsolutePath().resolve("target/classes/" + cName + ".properties");
        Path filePath;
        if (p0.toFile().exists()){
            filePath = p0;
        } else if (p1.toFile().exists()) {
            filePath = p1;
        } else if (p2.toFile().exists()) {
            filePath = p2;
        } else if (p3.toFile().exists()) {
            filePath = p3;
        } else {
            logger.error("Properties not located here:\n" +
                    p0.toString() + "\n" +
                    p1.toString() + "\n" +
                    p2.toString() + "\n" +
                    p3.toString());
            filePath = Paths.get("--");
        }
        //logger.debug(cName + " Properties path: " + filePath);
        return filePath;
    }
    public static Properties getProperties(String cName) {
        // Try to find and load cName.properties
        Properties prop = new Properties();
        try {
            InputStream in = new FileInputStream(getPropertiesPath(cName).toFile());
            prop.load(in);
        } catch (IOException e) {
            logger.error("<--- SeFunc.getProperties ex --->");
            for (StackTraceElement ste: e.getStackTrace()) logger.error(ste.toString());
        }
        return prop;
    }

    public static String getValue (Properties prop, String key) {
        if (prop.getProperty(key)!= null) {
            return prop.getProperty(key);
        } else {
            throw new NullPointerException("Key: " + key + " is null");
        }
    }
}