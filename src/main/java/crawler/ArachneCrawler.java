package crawler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import seeds.*;

public class ArachneCrawler extends Thread {
    private Properties arachneProps;
    private static String arachnePropsFile = "arachne.properties";
    
    private static String SINGLE_PASS = "single.pass";
    private static String SEEDS_SLEEP = "sleep.between.executions.ms";
    private static String DRIVER_PATH = "driver.path";
    private static String DRIVER_NAME = "driver.name";
    
    private boolean singlePass;
    private long seedSleep;
    private List<Seed> seeds = new ArrayList<Seed>();
    private List<String> pagesProcessed = new ArrayList<String>();
    private List<String> pagesToVisit;
    private List<String> pagesToProcess;
    private long startTime;
    
    private ArachneController controller;
    private WebDriver driver;
    private WebDriverWait waitShort;
    private WebDriverWait waitLong;
    
    private static Logger logger = Logger.getLogger(ArachneCrawler.class);
    
    public ArachneCrawler(ArachneController controller) {
        this.controller = controller;
        initializeWebDriver();
        initializeProperties();
    }
    
    public void run() {
        while (true) {
            startTime = System.currentTimeMillis();
            for (Seed currentSeed : seeds) {
                pagesToVisit = new LinkedList<String>();
                pagesToProcess = new ArrayList<String>();
                
                for (String url : currentSeed.getSeedUrls()) {
                    pagesToVisit.add(url);
                    logger.info("Added "+url+" to pagesToVisit");
                }
                
                while (CollectionUtils.isNotEmpty(pagesToVisit)) {
                    try {
                        String url = pagesToVisit.remove(0);
                        logger.info("Driver getting "+url);
                        driver.get(url);
                        
                        processNewLinks(currentSeed, url);
                        processResults(currentSeed);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage());
                    }
                }
            }
            logger.info("Completed all seeds. Elapsed time: "+getDurationBreakdown(System.currentTimeMillis()-startTime));
            try {
                Thread.sleep(seedSleep);
            } catch (InterruptedException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }
    
    private void processNewLinks(Seed currentSeed, String url) {
        List<WebElement> newLinks = waitLong.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("a")));
        HashSet<String> newUrls = new HashSet<String>();
        
        for (WebElement newLink : newLinks) {
            String link = newLink.getAttribute("href");
            if (link != null) {
                newUrls.add(link);
            }
        }
        logger.info("Found "+newUrls.size()+" new links at "+url);
        
        for (String newUrl : newUrls) {
            Matcher shouldVisit = Pattern.compile(currentSeed.getShouldVisitRegex()).matcher(newUrl);
            if (shouldVisit.matches()) {
                pagesToVisit.add(url);
            }
            
            Matcher shouldProcess = Pattern.compile(currentSeed.getShouldProcessRegex()).matcher(newUrl);
            boolean processMatches = shouldProcess.matches();
            if (processMatches && (!singlePass || (singlePass && notProcessed(newUrl)))) {
                pagesToProcess.add(shouldProcess.group(1));
            }
        }
        
        logger.info("Finished sorting new links.");
    }
    
    private void processResults(Seed currentSeed) throws Exception {
        String topic = currentSeed.getTopic();
        for (String page : pagesToProcess) {
            try {
                logger.info("Driver getting "+page);
                driver.get(page);
                logger.info("Starting to process "+page);
                
                String result = currentSeed.processResult(driver, waitShort, waitLong);
                if (result != null) {
                    controller.sendResult(result, topic);
                } else {
                    logger.info("Seed returned null result (Probably invalid category).");
                }
                if (singlePass) {
                    pagesProcessed.add(page);
                }
            } catch (Exception e) {
                logger.error("Unexpected error occured while processing results.",e);
            } 
        }
    }
    
    private void initializeWebDriver() {
        System.setProperty(DRIVER_NAME, DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        
        driver = new ChromeDriver(options);
        waitShort = new WebDriverWait(driver, 3);
        waitLong = new WebDriverWait(driver, 30);
    }
    
    private void initializeProperties() {
        arachneProps = new Properties();
        try {
            InputStream input = ArachneCrawler.class.getClassLoader().getResourceAsStream(arachnePropsFile);
            arachneProps.load(input);
            
            seedSleep = Long.parseLong(arachneProps.getProperty(SEEDS_SLEEP));
            singlePass = Boolean.parseBoolean(arachneProps.getProperty(SINGLE_PASS));
        } catch (FileNotFoundException e) {
            logger.error("Unable to load arachne properties file.",e);
        } catch (IOException e) {
            logger.error("Unexpected error occured while loading arachne properties.", e);;
        }
    }
    
    private boolean notProcessed(String newUrl) {
        for (String url : pagesProcessed) {
            if (StringUtils.contains(newUrl, url) ) {
                return false;
            }
        }
        return true;
    }
    
    public void addSeed(Seed seed) {
        seeds.add(seed);
    }
    
    public static String getDurationBreakdown(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(days);
        sb.append(" Days ");
        sb.append(hours);
        sb.append(" Hours ");
        sb.append(minutes);
        sb.append(" Minutes ");
        sb.append(seconds);
        sb.append(" Seconds");

        return(sb.toString());
    }
}
