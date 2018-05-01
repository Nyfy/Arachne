package seed;

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
            Actions actions = new Actions(driver);
            
            //This price is hard to grab, but we try before moving on
            try {
                actions.moveToElement(breadCrumbs.get(0));
                WebElement price = waitShort.until(ExpectedConditions.visibilityOfElementLocated(By.className("price-current")));
                if (price != null && StringUtils.isNotEmpty(price.getText())) {
                    result.put(Fields.PRICE, price.getText());
                }
            } catch (Exception e) {}
            
            WebElement details = waitShort.until(ExpectedConditions.visibilityOfElementLocated(By.id("Details_Tab")));
            actions.moveToElement(details).click().perform();
            
            result.put(Fields.URL, driver.getCurrentUrl());
            result.put(Fields.FOUNDTIME, Long.toString(System.currentTimeMillis()));
            result.put(Fields.CATEGORY, Fields.DISPLAY);
            
            List<WebElement> specs = waitLong.until(ExpectedConditions.visibilityOfElementLocated(By.id("Specs"))).findElements(By.tagName("dl"));
            
            for (WebElement field : specs) {
                String fieldTitle = field.findElement(By.tagName("dt")).getText();
                String fieldValue = field.findElement(By.tagName("dd")).getText();
                
                if (StringUtils.equalsIgnoreCase(fieldTitle, "brand")) {
                    result.put(Fields.BRAND, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "model")) {
                    result.put(Fields.MODEL, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "screen size")) {
                    result.put(Fields.SCREEN_SIZE, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "maximum resolution")) {
                    result.put(Fields.RESOLUTION, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "recommended resolution")) {
                    if (!result.containsKey(Fields.RESOLUTION)) {
                        result.put(Fields.RESOLUTION, fieldValue);
                    }
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "resolution")) {
                    if (!result.containsKey(Fields.RESOLUTION)) {
                        result.put(Fields.RESOLUTION, fieldValue);
                    }
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "response time")) {
                    result.put(Fields.RESPONSE_TIME, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "vertical refresh rate")) {
                    result.put(Fields.REFRESH_RATE, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "refresh rate")) {
                    if (!result.containsKey(Fields.REFRESH_RATE)) {
                        result.put(Fields.REFRESH_RATE, fieldValue);
                    }
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "panel")) {
                    result.put(Fields.PANEL_TYPE, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "adaptive sync technology")) {
                    result.put(Fields.ADAPTIVE_SYNC, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "d-sub")) {
                    result.put(Fields.DVI, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "vga")) {
                    result.put(Fields.VGA, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "dvi")) {
                    result.put(Fields.DVI, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "hdmi")) {
                    result.put(Fields.HDMI, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "displayport")) {
                    result.put(Fields.DISPLAY_PORT, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "vesa compatibility - mountable")) {
                    result.put(Fields.VESA_MOUNT, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "aspect ratio")) {
                    result.put(Fields.ASPECT_RATIO, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "pixel pitch")) {
                    result.put(Fields.PIXEL_PITCH, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "curved surface screen")) {
                    String curvature = result.get(Fields.CURVATURE);
                    if (curvature != null) {
                        result.put(Fields.CURVATURE,curvature + " " + fieldValue);
                    } else {
                        result.put(Fields.CURVATURE, fieldValue);
                    }
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "curvature radius")) {
                    result.put(Fields.CURVATURE, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "connectors")) {
                    result.put(Fields.CONNECTORS, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "brightness")) {
                    result.put(Fields.BRIGHTNESS, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "stand adjustments")) {
                    result.put(Fields.ERGONOMICS, fieldValue);
                } else if (StringUtils.equalsIgnoreCase(fieldTitle, "contrast ratio")) {
                    result.put(Fields.CONTRAST, fieldValue);
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
