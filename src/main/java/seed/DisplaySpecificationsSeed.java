package seed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import config.KafkaConfig;
import fields.Fields;

public class DisplaySpecificationsSeed extends Seed {
    
    public DisplaySpecificationsSeed() {
        String shouldVisitRegex = "^[^\\/]+\\/\\/[^\\/]+\\/en\\/brand\\/.*$";
        String shouldProcessRegex = "^([^\\/]+\\/\\/[^\\/]+\\/en\\/model\\/.*)$";
        
        initializeSeedUrls();
        setShouldVisitRegex(shouldVisitRegex);
        setShouldProcessRegex(shouldProcessRegex);
        setTopic(KafkaConfig.DISPLAY_SOURCE_TOPIC);
    }
    
    @Override
    public String processResult(WebDriver driver, WebDriverWait waitShort, WebDriverWait waitLong) throws JsonProcessingException {
        Map<String,String> result = new HashMap<String,String>();
        ObjectMapper objectMapper = new ObjectMapper();
        
        List<WebElement> specTables = driver.findElements(By.tagName("tbody"));
        
        result.put(Fields.URL, driver.getCurrentUrl());
        result.put(Fields.FOUNDTIME, Long.toString(System.currentTimeMillis()));
        result.put(Fields.CATEGORY, Fields.DISPLAY);
        
        for (WebElement specTable : specTables) {
            List<WebElement> specRows = specTable.findElements(By.tagName("tr"));
            for (WebElement specRow : specRows) {
                List<WebElement> specValues = specRow.findElements(By.tagName("td"));
                String specName = StringUtils.substringBefore(specValues.get(0).getText(), "\n");
                String specValue = specValues.get(1).getText();
                
                if (StringUtils.equalsIgnoreCase(specName, "brand")) {
                    result.put(Fields.BRAND, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "model")) {
                    result.put(Fields.MODEL, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "size class")) {
                    result.put(Fields.SCREEN_SIZE, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "resolution")) {
                    result.put(Fields.RESOLUTION, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "minimum response time")) {
                    result.put(Fields.RESPONSE_TIME, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "vertical frequency")) {
                    result.put(Fields.REFRESH_RATE, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "panel type")) {
                    result.put(Fields.PANEL_TYPE, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "connectivity")) {
                    result.put(Fields.CONNECTORS, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "vesa mount")) {
                    String vesa1 = result.get(Fields.VESA_MOUNT);
                    if (vesa1 != null) {
                        result.put(Fields.VESA_MOUNT, vesa1 + " " + specValue);
                    } else {
                        result.put(Fields.VESA_MOUNT, specValue);
                    }
                } else if (StringUtils.equalsIgnoreCase(specName, "vesa interface")) {
                    String vesa2 = result.get(Fields.VESA_MOUNT);
                    if (vesa2 != null) {
                        result.put(Fields.VESA_MOUNT, vesa2 + " " + specValue);
                    } else {
                        result.put(Fields.VESA_MOUNT, specValue);
                    }
                } else if (StringUtils.equalsIgnoreCase(specName, "aspect ratio")) {
                    result.put(Fields.ASPECT_RATIO, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "pixel pitch")) {
                    result.put(Fields.PIXEL_PITCH, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "pixel density")) {
                    result.put(Fields.PIXEL_DENSITY, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "brightness")) {
                    result.put(Fields.BRIGHTNESS, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "removable stand")) {
                    result.put(Fields.REMOVABLE_STAND, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "height adjustment")) {
                    String height1 = result.get(Fields.HEIGHT_ADJUSTMENT);
                    if (height1 != null) {
                        result.put(Fields.HEIGHT_ADJUSTMENT,height1 + specValue);
                    } else {
                        result.put(Fields.HEIGHT_ADJUSTMENT, specValue);
                    }
                } else if (StringUtils.equalsIgnoreCase(specName, "height adjustment range")) {
                    String height2 = result.get(Fields.HEIGHT_ADJUSTMENT);
                    if (height2 != null) {
                        result.put(Fields.HEIGHT_ADJUSTMENT,height2 + specValue);
                    } else {
                        result.put(Fields.HEIGHT_ADJUSTMENT, specValue);
                    }
                } else if (StringUtils.equalsIgnoreCase(specName, "landscape/portrait pivot")) {
                    result.put(Fields.PIVOT_ADJUSTMENT, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "left/right swivel")) {
                    result.put(Fields.SWIVEL_ADJUSTMENT, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "forward/backward tilt")) {
                    result.put(Fields.TILT_ADJUSTMENT, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "forward tilt")) {
                    result.put(Fields.FORWARD_TILT, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "backward tilt")) {
                    result.put(Fields.BACKWARD_TILT, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "radius of curvature")) {
                    result.put(Fields.CURVATURE, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "display area")) {
                    result.put(Fields.DISPLAY_AREA, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "left swivel")) {
                    result.put(Fields.LEFT_SWIVEL, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "right swivel")) {
                    result.put(Fields.RIGHT_SWIVEL, specValue);
                } else if (StringUtils.equalsIgnoreCase(specName, "dynamic contrast")) {
                    result.put(Fields.DYNAMIC_CONTRAST, specValue);
                }  else if (StringUtils.equalsIgnoreCase(specName, "static contrast")) {
                    result.put(Fields.STATIC_CONTRAST, specValue);
                }
            }
        }
        return objectMapper.writeValueAsString(result);
    }
    
    private void initializeSeedUrls() {
        List<String> seedUrls = new ArrayList<String>();
        
        seedUrls.add("https://www.displayspecifications.com/en");
        
        setSeedUrls(seedUrls);
    }
}
