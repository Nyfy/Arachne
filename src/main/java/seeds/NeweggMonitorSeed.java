package seeds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.databind.ObjectMapper;

import config.Fields;
import config.KafkaConfig;

public class NeweggMonitorSeed extends Seed {
    String shouldVisitRegex = ".*\\/LCD-LED-Monitors\\/SubCategory\\/ID-20\\/Page-.*";
    String shouldProcessRegex = "(.*\\/Product\\/Product.aspx\\?Item=[^&]*).*";
    
    public NeweggMonitorSeed() {
        initializeSeedUrls();
        setShouldVisitRegex(shouldVisitRegex);
        setShouldProcessRegex(shouldProcessRegex);
        setTopic(KafkaConfig.MONITOR_SOURCE_TOPIC);
    }
    
    private void initializeSeedUrls() {
        List<String> seedUrls = new ArrayList<String>();
        for (int i = 1; i < 25; i++) {
            seedUrls.add("https://www.newegg.ca/LCD-LED-Monitors/SubCategory/ID-20/Page-"+Integer.toString(i)+"?order=BESTMATCH&PageSize=96");
        }
        setSeedUrls(seedUrls);
    }
    
    @Override
    public String processResult(WebDriver driver, WebDriverWait waitShort, WebDriverWait waitLong) throws Exception {
        boolean isMonitor = false;
        
        List<WebElement> breadCrumbs = waitLong.until(ExpectedConditions.visibilityOfElementLocated(By.className("breadcrumb"))).findElements(By.tagName("a"));
        
        for (WebElement link : breadCrumbs) {
            if (StringUtils.containsIgnoreCase(link.getText(),"LCD / LED Monitors")) {
                isMonitor = true;
            }
        }
        
        if (isMonitor) {
            Map<String,String> result = new HashMap<String,String>();
            ObjectMapper objectMapper = new ObjectMapper();
            
            //This price is hard to grab, but we try before moving on
            try {
                WebElement price = waitShort.until(ExpectedConditions.visibilityOfElementLocated(By.className("price-current")));
                result.put(Fields.PRICE, price.getText());
            } catch (Exception e) {}
            
            WebElement details = waitShort.until(ExpectedConditions.visibilityOfElementLocated(By.id("Details_Tab")));
            Actions actions = new Actions(driver);
            actions.moveToElement(details).click().perform();
            
            result.put(Fields.URL, driver.getCurrentUrl());
            result.put(Fields.FOUNDTIME, Long.toString(System.currentTimeMillis()));
            result.put(Fields.CATEGORY, Fields.MONITOR);
            
            List<WebElement> specs = waitLong.until(ExpectedConditions.visibilityOfElementLocated(By.id("Specs"))).findElements(By.tagName("dl"));
            
            for (WebElement field : specs) {
                WebElement fieldTitleElement = field.findElement(By.tagName("dt"));
                String fieldTitle = fieldTitleElement.getText();
                if (StringUtils.containsIgnoreCase(fieldTitle, "brand")) {
                    result.put(Fields.BRAND, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "model")) {
                    result.put(Fields.MODEL, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "screen size")) {
                    result.put(Fields.SCREEN_SIZE, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "maximum resolution")) {
                    result.put(Fields.RESOLUTION, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "recommended resolution") && !result.containsKey(Fields.RESOLUTION)) {
                    result.put(Fields.RESOLUTION, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "resolution") && !result.containsKey(Fields.RESOLUTION)) {
                    result.put(Fields.RESOLUTION, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "response time")) {
                    result.put(Fields.RESPONSE_TIME, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "vertical refresh rate")) {
                    result.put(Fields.REFRESH_RATE, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "refresh rate") && !result.containsKey(Fields.REFRESH_RATE)) {
                    result.put(Fields.REFRESH_RATE, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "panel")) {
                    result.put(Fields.PANEL_TYPE, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "adaptive sync")) {
                    result.put(Fields.ADAPTIVE_SYNC, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "d-sub")) {
                    result.put(Fields.VGA, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "vga")) {
                    result.put(Fields.VGA, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "dvi")) {
                    result.put(Fields.DVI, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "hdmi")) {
                    result.put(Fields.HDMI, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "displayport")) {
                    result.put(Fields.DISPLAY_PORT, field.findElement(By.tagName("dd")).getText());
                } else if (StringUtils.containsIgnoreCase(fieldTitle, "vesa")) {
                    result.put(Fields.VESA, field.findElement(By.tagName("dd")).getText());
                }
            }
            return objectMapper.writeValueAsString(result);
        }
        return null;
    }
}