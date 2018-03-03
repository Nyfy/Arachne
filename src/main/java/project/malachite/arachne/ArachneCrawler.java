package project.malachite.arachne;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ArachneCrawler extends Thread {
    private ArachneController controller;
    
    private List<Seed> seeds = new ArrayList<Seed>();
    private List<String> pagesToVisit;
    
    public ArachneCrawler(ArachneController controller) {
        this.controller = controller;
    }
    
    public void addSeed(Seed seed) {
        seeds.add(seed);
    }
    
    public void run() {
        System.setProperty("webdriver.chrome.driver", "/home/marc/chromedriver");
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("headless");
        //options.addArguments("-allow-running-insecure-content");
        
        WebDriver driver = new ChromeDriver(options);

        while (true) {
            for (Seed currentSeed : seeds) {
                pagesToVisit = new ArrayList<String>();
                pagesToVisit.addAll(currentSeed.getSeedUrls());
                
                while (CollectionUtils.isNotEmpty(pagesToVisit)) {
                    String url = pagesToVisit.remove(0);
                    driver.get(url);
                    
                    pagesToVisit.addAll(currentSeed.processHyperlinks(driver));
                    Matcher shouldProcess = Pattern.compile(currentSeed.getShouldProcessRegex()).matcher(url);
                    
                    if (shouldProcess.find()) {
                        try {
                            controller.sendResult(currentSeed.processResult(url, driver), currentSeed.getTopic());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
