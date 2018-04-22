package seeds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.databind.ObjectMapper;

import fields.Fields;
import config.KafkaConfig;

public class NeweggMonitorSeed extends Seed {
    
    public NeweggMonitorSeed() {
        String shouldVisitRegex = "^.*\\/LCD-LED-Monitors\\/SubCategory\\/ID-20\\/Page-.*$";
        String shouldProcessRegex = "^(.*\\/Product\\/Product.aspx\\?Item=[^&]*).*$";
        
        initializeSeedUrls();
        setShouldVisitRegex(shouldVisitRegex);
        setShouldProcessRegex(shouldProcessRegex);
        setTopic(KafkaConfig.DISPLAY_SOURCE_TOPIC);
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
                if (price != null && StringUtils.isNotEmpty(price.getText())) {
                    result.put(Fields.PRICE, price.getText());
                }
            } catch (Exception e) {}
            
            WebElement details = waitShort.until(ExpectedConditions.visibilityOfElementLocated(By.id("Details_Tab")));
            Actions actions = new Actions(driver);
            actions.moveToElement(details).click().perform();
            
            result.put(Fields.URL, driver.getCurrentUrl());
            result.put(Fields.FOUNDTIME, Long.toString(System.currentTimeMillis()));
            result.put(Fields.CATEGORY, Fields.DISPLAY);
            
            List<WebElement> specs = waitLong.until(ExpectedConditions.visibilityOfElementLocated(By.id("Specs"))).findElements(By.tagName("dl"));
            
            for (WebElement field : specs) {
                WebElement fieldTitleElement = field.findElement(By.tagName("dt"));
                String fieldTitle = fieldTitleElement.getText();
                
                String fieldValue = field.findElement(By.tagName("dd")).getText();
                
                switch (StringUtils.lowerCase(fieldTitle)) {
                    case "brand": result.put(Fields.BRAND, fieldValue);
                    case "model": result.put(Fields.MODEL, fieldValue);
                    case "screen size": result.put(Fields.SCREEN_SIZE, fieldValue);
                    case "maximum resolution": result.put(Fields.RESOLUTION, fieldValue);
                    case "recommended resolution": 
                        if (!result.containsKey(Fields.RESOLUTION)) {
                            result.put(Fields.BRAND, fieldValue);
                        }
                    case "resolution": 
                        if (!result.containsKey(Fields.RESOLUTION)) {
                            result.put(Fields.RESOLUTION, fieldValue);
                        }
                    case "response time": result.put(Fields.RESPONSE_TIME, fieldValue);
                    case "vertical refresh rate": result.put(Fields.REFRESH_RATE, fieldValue);
                    case "refresh rate": 
                        if (!result.containsKey(Fields.REFRESH_RATE)) {
                            result.put(Fields.REFRESH_RATE, fieldValue);
                        }
                    case "panel": result.put(Fields.PANEL_TYPE, fieldValue);
                    case "adaptive sync technology": result.put(Fields.ADAPTIVE_SYNC, fieldValue);
                    case "d-sub": result.put(Fields.DVI, fieldValue);
                    case "vga": result.put(Fields.VGA, fieldValue);
                    case "dvi": result.put(Fields.DVI, fieldValue);
                    case "hdmi": result.put(Fields.HDMI, fieldValue);
                    case "displayport": result.put(Fields.DISPLAY_PORT, fieldValue);
                    case "vesa compatibility - mountable": result.put(Fields.VESA_MOUNT, fieldValue);
                    case "aspect ratio": result.put(Fields.ASPECT_RATIO, fieldValue);
                    case "pixel pitch": result.put(Fields.PIXEL_PITCH, fieldValue);
                    case "curved surface screen": 
                        String curvature = result.get(Fields.CURVATURE);
                        if (curvature != null) {
                            result.put(Fields.CURVATURE,curvature + " " + fieldValue);
                        } else {
                            result.put(Fields.CURVATURE, fieldValue);
                        }
                    case "curvature radius": result.put(Fields.CURVATURE, fieldValue);
                    case "connectors": result.put(Fields.CONNECTORS, fieldValue);
                    case "brightness": result.put(Fields.BRIGHTNESS, fieldValue);
                    case "stand adjustments": result.put(Fields.ERGONOMICS, fieldValue);
                }
            }
            return objectMapper.writeValueAsString(result);
        }
        return null;
    }
    
    private void initializeSeedUrls() {
        List<String> seedUrls = new ArrayList<String>();
        for (int i = 1; i < 25; i++) {
            seedUrls.add("https://www.newegg.ca/LCD-LED-Monitors/SubCategory/ID-20/Page-"+Integer.toString(i)+"?order=BESTMATCH&PageSize=96");
        }
        setSeedUrls(seedUrls);
    }
}
