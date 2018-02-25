package project.malachite.arachne;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ArachneCrawler extends Thread {
    
    private ArachneController controller;
    
    private List<String> pagesToVisit;
    private String threadName;
    private String shouldVisitRegex;
    private String shouldProcessRegex;
    
    public ArachneCrawler(String threadName, String shouldVisitRegex, String shouldProcessRegex,ArachneController controller) {
        pagesToVisit = new ArrayList<String>();
        this.controller = controller;
        this.threadName = threadName;
        this.shouldVisitRegex = shouldVisitRegex;
        this.shouldProcessRegex = shouldProcessRegex;
    }
    
    public void addNextUrl (String url) {
        if (StringUtils.isNotEmpty(url)) {
            pagesToVisit.add(url);
        }
    }
    
    public int getLoad() {
        return pagesToVisit.size();
    }
    
    public void run() {
        String url;
        while (true) {
            if (CollectionUtils.isNotEmpty(pagesToVisit)) {
                url = pagesToVisit.remove(0);
                
                WebDriver chromeDriver = new ChromeDriver();
                chromeDriver.get(url);
                
                processHyperlinks(chromeDriver); 
                
                Matcher shouldProcess = Pattern.compile(shouldProcessRegex).matcher(url);
                if (shouldProcess.find()) {
                    processResult(url,chromeDriver);
                }
                chromeDriver.close();
            }
        }
    }
    
    private void processHyperlinks(WebDriver driver) {
        List<WebElement> newLinks = driver.findElements(By.tagName("a"));
        for (WebElement newLink : newLinks) {
            String newUrl = newLink.getAttribute("href");
            
            if (newUrl != null) {
                Matcher shouldVisit = Pattern.compile(shouldVisitRegex).matcher(newUrl);
                
                if (shouldVisit.find()) {
                    pagesToVisit.add(newUrl);
                }
            }
        }
    }
    
    private void processResult(String url, WebDriver driver) {
        String title = null;
        String price = null;
        String description = null;
        String region = null;
        String address = null;
        String visits = null;
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        if (StringUtils.contains(url, "kijiji")) {
            title = driver.findElement(By.className("title-3283765216")).getText();
            price = driver.findElement(By.className("currentPrice-2872355490")).getText();
            description = driver.findElement(By.className("descriptionContainer-2832520341")).getText();
            address = driver.findElement(By.className("address-2932131783")).getText();
            visits = driver.findElement(By.className("visitCounter-450272408")).getText();
            try {
                String result = objectMapper.writeValueAsString(new ArachnePost(title,price,description,region,address,visits));
                controller.sendResult(result);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
