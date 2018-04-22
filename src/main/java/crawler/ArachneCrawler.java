package crawler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import seeds.*;

public class ArachneCrawler extends Thread {
    private Properties arachneProps;
    private static String arachnePropsFile = "arachne.properties";
    
    private static String CONTINUOUS = "continuous";
    private static String SINGLE_PASS = "single.pass";
    private static String SEEDS_SLEEP = "sleep.between.executions.ms";
    private static String DRIVER_PATH = "driver.path";
    private static String DRIVER_NAME = "driver.name";
    
    private static boolean stopped = false;
    
    private boolean singlePass;
    private boolean continuous;
    private long repeatDelay;
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
    
    protected void setupTest(WebDriver mockWebDriver, WebDriverWait mockWaitShort, 
            WebDriverWait mockWaitLong, List<String> testPagesToVisit, 
            List<String> testPagesToProcess, List<String> testPagesProcessed) {
        
        pagesToProcess = testPagesToProcess;
        pagesToVisit = testPagesToVisit;
        pagesProcessed = testPagesProcessed;
        
        initializeWebDriver(mockWebDriver);
        initializeDriverWaits(mockWaitShort, mockWaitLong);
    }
    
    public ArachneCrawler(ArachneController controller, boolean initializeDriver) {
        setController(controller);
        initializeCrawlerProperties();
        
        if (initializeDriver) {
            initializeWebDriver(null);
            initializeDriverWaits(null,null);
        }
    }
    
    public void run() {
        if (!stopped) {
            do {
                startTime = System.currentTimeMillis();
                for (Seed currentSeed : seeds) {
                    setupSeed(currentSeed);
                    processSeed(currentSeed);
                }
                logger.info("Completed all seeds. Elapsed time: "+getDurationBreakdown(System.currentTimeMillis()-startTime));
                
                try {
                    Thread.sleep(repeatDelay);
                } catch (InterruptedException e) {
                    logger.fatal(e.getLocalizedMessage());
                    stopped = true;
                    return;
                }
            } while (continuous);
        }
    }
    
    private void setupSeed(Seed seed) {
        pagesToVisit = new LinkedList<String>();
        pagesToProcess = new ArrayList<String>();
        
        for (String url : seed.getSeedUrls()) {
            pagesToVisit.add(url);
            logger.info("Added "+url+" to pagesToVisit");
        }
    }
    
    private void processSeed(Seed seed) {
        int tries = 5;
        while (CollectionUtils.isNotEmpty(pagesToVisit)) {
            try {
                String url = pagesToVisit.remove(0);
                logger.info("Driver getting "+url);
                driver.get(url);
                
                sortNewLinks(seed, url);
                processResults(seed);
            } catch (WebDriverException e) {
                // WebDriver crashed, start a new session
                if (tries > 0) {
                    logger.error("WebDriver exception caught, reinitializing.", e);
                    initializeWebDriver(null);
                    initializeDriverWaits(null,null);
                    
                    tries--;
                    logger.info("WebDriver reinitialized, "+tries+" tries left.");
                } else {
                    return;
                }
            } catch (Exception e) {
                logger.error("Unexpected error occured while processing seed.", e);
            }
        }
    }
    
    private void sortNewLinks(Seed currentSeed, String url) {
        Set<String> newUrls = getNewUrls();
        logger.info("Found "+newUrls.size()+" new links at "+url);
        
        for (String newUrl : newUrls) {
            if (shouldVisit(currentSeed, newUrl)) {
                pagesToVisit.add(newUrl);
            }
            
            String shouldProcessUrl = shouldProcess(currentSeed, newUrl);
            if (shouldProcessUrl != null) {
                pagesToProcess.add(shouldProcessUrl);
            }
        }
        logger.info("Finished sorting new links.");
    }
    
    protected boolean shouldVisit(Seed currentSeed, String newUrl) {
        Matcher shouldVisit = Pattern.compile(currentSeed.getShouldVisitRegex()).matcher(newUrl);
        return shouldVisit.matches();
    }
    
    protected String shouldProcess(Seed currentSeed, String newUrl) {
        Matcher shouldProcess = Pattern.compile(currentSeed.getShouldProcessRegex()).matcher(newUrl);
        boolean processMatches = shouldProcess.matches();
        if (processMatches && (!singlePass || (singlePass && notProcessed(newUrl)))) {
            return shouldProcess.group(1);
        } else {
            return null;
        }
    }
    
    private Set<String> getNewUrls() {
        List<WebElement> newLinks = waitLong.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("a")));
        
        HashSet<String> newUrls = new HashSet<String>();
        for (WebElement newLink : newLinks) {
            try {
                String link = newLink.getAttribute("href");
                if (link != null) {
                    newUrls.add(link);
                }
            } catch (StaleElementReferenceException e) {
                logger.error("StaleElement occured while processing new links, skipping to next link.");
            }
        }
        return newUrls;
    }
    
    protected void processResults(Seed currentSeed) {
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
                logger.error("Unexpected error occured while processing results.", e);
            }
        }
    }
    
    private void initializeWebDriver(WebDriver webDriver) {
        if (webDriver == null) {
            System.setProperty(DRIVER_NAME, DRIVER_PATH);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("headless");
            
            driver = new ChromeDriver(options);
        } else {
            driver = webDriver;
        }
    }
    
    private void initializeDriverWaits(WebDriverWait driverWaitShort, WebDriverWait driverWaitLong) {
        if (driverWaitShort == null) {
            waitShort = new WebDriverWait(driver, 5);
        } else {
            waitShort = driverWaitShort;
        }
        
        if (driverWaitLong == null) {
            waitLong = new WebDriverWait(driver, 30);
        } else {
            waitLong = driverWaitLong;
        }
    }
    
    private void initializeCrawlerProperties() {
        arachneProps = new Properties();
        try {
            
            InputStream inputProps = ArachneCrawler.class.getClassLoader().getResourceAsStream(arachnePropsFile);
            
            arachneProps.load(inputProps);
            
            if (inputProps != null) {
                inputProps.close();
            }
            
            repeatDelay = Long.parseLong(arachneProps.getProperty(SEEDS_SLEEP));
            singlePass = Boolean.parseBoolean(arachneProps.getProperty(SINGLE_PASS));
            continuous = Boolean.parseBoolean(arachneProps.getProperty(CONTINUOUS));
        } catch (Exception e) {
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
    
    private void setController(ArachneController controller) {
        this.controller = controller;
    }
    
    protected void addSeed(Seed seed) {
        seeds.add(seed);
    }
    
    private static String getDurationBreakdown(long millis) {
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
