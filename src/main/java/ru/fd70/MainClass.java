// decoder(String[]) позволяет использовать выражения: 1x5 --> 1 1 1 1 1
package ru.fd70;
import static ru.fd70.funcs.SeFunc.getPropertiesPath;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public final class MainClass {
    private static final int COUNT_OF_TESTS = 9;
    private static final int COUNT_OF_RUNNING_THREADS = 10;

    private static class QueueController {
        Queue<Integer> processCases;
        LinkedList<TestProcess> testProcessList = new LinkedList<>();
        private boolean INPUT_IS_OPEN = false;

        QueueController(Queue<Integer> pc) {
            this.processCases = pc;
        }

        void startWatching() {
            INPUT_IS_OPEN = true;
            while (INPUT_IS_OPEN || (processCases.size() != 0) || (testProcessList.size() != 0)) {
                Thread.yield();
                if ((processCases.size() != 0) && (testProcessList.size() < COUNT_OF_RUNNING_THREADS)) {
                    //testProcessList.add(new TestProcess(processCases.poll()));
                    TestProcess tp = new TestProcess(processCases.poll());
                    testProcessList.add(tp);
                    tp.start();
                }
                for (int i = 0; i < testProcessList.size(); i++) {
                    //clear closed processes/ вынести в отдельный метод?
                    TestProcess testProcess = testProcessList.get(i);
                    if (!testProcess.isAlive()) {
                        testProcessList.remove(i);
                    }
                }
            }
        }
        void stopWatching() {
            this.INPUT_IS_OPEN = false;
        }
    }
    private static class TestProcess extends Thread {
        int caseNum;
        TestProcess(int CDFactorycase) {
            caseNum = CDFactorycase;
        }

        @Override
        public void run() {
            new Thread(CDFactory.main(caseNum)).start();
        }
    }

    private static String repeat(int count, String with) {
        return new String(new char[count]).replace("\0", with);
    }
    private static String decoder(String[] args) {
        //decoder(String[]) позволяет использовать выражения: 1x5 --> 1 1 1 1 1
        StringBuilder output = new StringBuilder();
        for (String argument:args) {
            if (argument.toLowerCase().equals("all")) {
                for (int i = 1; i <= COUNT_OF_TESTS; i++) {
                    output.append(" ").append(i);
                }
                output.append(" 0");
                return output.toString();
            }
            if (argument.equals("0") || argument.equals("exit")) {
                output.append(" ").append(argument);
                return output.toString();
            }
            if (argument.contains("x")) {
                //String separator = "x"; позволяет использовать выражения: 1x5 --> 1 1 1 1 1
                LinkedList<String> ll = new LinkedList<>(Arrays.asList(argument.split("x")));
                if (ll.size() == 2) {
                    try {
                        int int1 = Integer.parseInt(ll.get(0));
                        int int2 = Integer.parseInt(ll.get(1));
                        output.append(repeat(int2, " " + int1));
                    } catch (NumberFormatException e) {
                        logger.warn("Wrong argument: " + argument);
                    }
                } else {
                    logger.warn("Wrong argument: " + argument);
                }
                continue;
            }
            try {
                int itmustbeinteger = Integer.parseInt(argument);
                output.append(" ").append(itmustbeinteger);
            } catch (NumberFormatException e) {
                logger.warn("Wrong argument: " + argument);
            }
        }
        return output.toString();
    }
    private static String decoder(String arg) {
        return decoder(arg.split(" "));
    }
    private static Scanner systemin() {
        // System.in пропущенный через MainClass.decoder()
        Scanner scan = new Scanner(System.in);
        scan = new Scanner(decoder(String.valueOf(scan.nextLine())));
        return scan;
    }

    private static final Logger logger = LoggerFactory.getLogger("main");
    private static final Queue<Integer> testProcessOrder = new LinkedList<>();

    private static void mainLoop (Scanner scan) {
        while ((!scan.hasNext("exit")) && (!scan.hasNext("0"))) {
            if (scan.hasNextInt()) {
                int nInt = scan.nextInt();
                //new Thread(() -> CDFactory.main(nInt)).start();
                testProcessOrder.add(nInt);
            } else {
                if (scan.hasNext()) {
                    //Если след. аргумент не число - пропустить.
                    scan.next();
                }
            }
            if (!scan.hasNext()) {
                // If no args more, read System.in
                scan = systemin();
            }
        }
    }
    public static void main (String[] args) {
        PropertyConfigurator.configure(getPropertiesPath("log4j").toString());

        QueueController queueController = new QueueController(testProcessOrder);
        new Thread(queueController::startWatching).start();

        logger.warn("\n\n<------------->");
        logger.warn(Date.from(Instant.now()).toString());
        logger.info("Input next line or separately. type 'exit' or '0' to quit:");

        Scanner scan = new Scanner(decoder(args));
        //if (!scan.hasNext()) scan = new Scanner(System.in);
        if (!scan.hasNext()) scan = systemin();

        try {
            mainLoop(scan);
            scan.close();
            queueController.stopWatching();
            logger.info("Input completed");
        } catch (Exception e) {
            scan.close();
            queueController.stopWatching();
            logger.error("<--- --- --->");
            logger.error(e.getCause() + e.getMessage());
            for (StackTraceElement ste: e.getStackTrace()) {
                if (ste.toString().contains("ru.cfmc.")) {
                    logger.error(ste.toString());
                }
            }
        }
    }
}