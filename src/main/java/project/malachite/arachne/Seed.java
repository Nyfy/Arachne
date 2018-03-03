package project.malachite.arachne;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Seed {
    private final String shouldVisitRegex;
    private final String shouldProcessRegex;
    private final String topic;
    private final List<String> seedUrls;
    
    public Seed(List<String> seedUrls, String shouldVisitRegex, String shouldProcessRegex, String topic) {
        this.seedUrls = seedUrls;
        this.shouldVisitRegex = shouldVisitRegex;
        this.topic = topic;
        this.shouldProcessRegex = shouldProcessRegex;
    }
    
    public List<String> getSeedUrls() {
        return seedUrls;
    }
    
    public String getShouldProcessRegex() {
        return shouldProcessRegex;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public List<String> processHyperlinks(WebDriver driver) {
        List<String> newPages = new ArrayList<String>();
        List<WebElement> newLinks = driver.findElements(By.tagName("a"));
        for (WebElement newLink : newLinks) {
            String newUrl = newLink.getAttribute("href");
            
            if (newUrl != null) {
                Matcher shouldVisit = Pattern.compile(shouldVisitRegex).matcher(newUrl);
                
                if (shouldVisit.find()) {
                    newPages.add(newUrl);
                }
            }
        }
        return newPages;
    }
    
    //Need to override to process specific sources
    public String processResult(String url, WebDriver driver) throws JsonProcessingException {
        return null;
    }
}
