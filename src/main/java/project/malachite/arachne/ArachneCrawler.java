package project.malachite.arachne;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ArachneCrawler extends Thread {
    private static String DRIVER_PATH = "/home/marc/chromedriver";
    private static String CHROME_DRIVER = "webdriver.chrome.driver";
    
    private ArachneController controller;
    
    private List<Seed> seeds = new ArrayList<Seed>();
    private List<String> pagesToVisit;
    private Set<String> pagesToProcess;
    private Set<String> pagesVisited;
    
    private WebDriver driver;
    
    public ArachneCrawler(ArachneController controller) {
        this.controller = controller;
    }
    
    public void addSeed(Seed seed) {
        seeds.add(seed);
    }
    
    public void run() {
        pagesVisited = new TreeSet<String>();
        initializeWebDriver();
        
        while (true) {
            for (Seed currentSeed : seeds) {
                pagesToVisit = new ArrayList<String>();
                pagesToProcess = new TreeSet<String>();
                
                for (String url : currentSeed.getSeedUrls()) {
                    pagesToVisit.add(url);
                }
                
                while (CollectionUtils.isNotEmpty(pagesToVisit)) {
                    String url = pagesToVisit.remove(0);
                    
                    if (!pagesVisited.contains(url)) {
                        driver.get(url);
                        processNewLinks(currentSeed);
                    }
                    processResults(currentSeed);
                }
            }
        }
    }
    
    private void initializeWebDriver() {
        System.setProperty(CHROME_DRIVER, DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        
        driver = new ChromeDriver(options);
    }
    
    private void processNewLinks(Seed currentSeed) {
        List<WebElement> newLinks = driver.findElements(By.tagName("a"));
        TreeSet<String> newUrls = new TreeSet<String>();
        
        for (WebElement newLink : newLinks) {
            if (newLink.getAttribute("href") != null) {
                newUrls.add(newLink.getAttribute("href"));
            }
        }
        
        for (String newUrl : newUrls) {
            if (newUrl != null) {
                for (String shouldVisit : currentSeed.getShouldVisitRegex()) {
                    Matcher visit = Pattern.compile(shouldVisit).matcher(newUrl);
                    if (visit.find()) {
                        pagesToVisit.add(newUrl);
                    }
                }
                Matcher process = Pattern.compile(currentSeed.getShouldProcessRegex()).matcher(newUrl);
                if (process.find()) {
                    pagesToProcess.add(newUrl);
                }
            }
        }
    }
    
    private void processResults(Seed currentSeed) {
        for (String page : pagesToProcess) {
            driver.get(page);
            try {
                String result = currentSeed.processResult(driver);
                if (result != null) {
                    controller.sendResult(result, currentSeed.getTopic());
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
        }
    }
}
