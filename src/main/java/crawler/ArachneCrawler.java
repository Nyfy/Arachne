package crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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

import com.fasterxml.jackson.core.JsonProcessingException;

import seed.Seed;

public class ArachneCrawler extends Thread {
    private static String DRIVER_PATH = "/home/marc/chromedriver";
    private static String CHROME_DRIVER = "webdriver.chrome.driver";
    
    private ArachneController controller;
    
    private List<Seed> seeds = new ArrayList<Seed>();
    private List<String> pagesToVisit;
    private List<String> pagesToProcess;
    private List<String> pagesProcessed;
    
    private long startTime;
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    private static Logger logger = Logger.getLogger(ArachneCrawler.class);
    
    public ArachneCrawler(ArachneController controller) {
        this.controller = controller;
    }
    
    public void addSeed(Seed seed) {
        seeds.add(seed);
    }
    
    public void run() {
        pagesProcessed = new ArrayList<String>();
        initializeWebDriver();
        
        while (true) {
            startTime = System.currentTimeMillis();
            for (Seed currentSeed : seeds) {
                pagesToVisit = new ArrayList<String>();
                pagesToProcess = new ArrayList<String>();
                
                for (String url : currentSeed.getSeedUrls()) {
                    pagesToVisit.add(url);
                    logger.info("Added "+url+" to pagesToVisit");
                }
                
                while (CollectionUtils.isNotEmpty(pagesToVisit)) {
                    try {
                        String url = pagesToVisit.remove(0);
                        logger.info("Starting processing for "+url);
                        driver.get(url);
                        
                        
                        processNewLinks(currentSeed);
                        processResults(currentSeed);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage());
                    }
                }
            }
            logger.info("Completed all seeds. Elapsed time: "+(System.currentTimeMillis()-startTime)+" ms");
            try {
                Thread.sleep(3600000L);
            } catch (InterruptedException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }
    
    private void initializeWebDriver() {
        System.setProperty(CHROME_DRIVER, DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 30);
    }
    
    private void processNewLinks(Seed currentSeed) {
        List<WebElement> newLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("a")));
        TreeSet<String> newUrls = new TreeSet<String>();
        
        for (WebElement newLink : newLinks) {
            String link = newLink.getAttribute("href");
            if (link != null) {
                newUrls.add(link);
            }
        }
        
        logger.info("Found "+newUrls.size()+" new links at "+driver.getCurrentUrl());
        
        for (String newUrl : newUrls) {
            if (newUrl != null) {
                Matcher visit = Pattern.compile(currentSeed.getShouldVisitRegex()).matcher(newUrl);
                if (visit.find()) {
                    pagesToVisit.add(newUrl);
                }
                
                Matcher process = Pattern.compile(currentSeed.getShouldProcessRegex()).matcher(newUrl);
                if (process.find() && notProcessed(newUrl)) {
                    pagesToProcess.add(newUrl);
                    pagesProcessed.add(newUrl);
                }
            }
        }
        
        logger.info("Finished sorting all new links.");
    }
    
    private void processResults(Seed currentSeed) throws Exception {
        for (String page : pagesToProcess) {
            try {
                driver.get(page);
                logger.info("Starting to process "+page);
                
                String result = currentSeed.processResult(driver, wait);
                if (result != null) {
                    controller.sendResult(result, currentSeed.getTopic());
                } else {
                    logger.info("Seed returned null result (Probably invalid category)");
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
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
}
