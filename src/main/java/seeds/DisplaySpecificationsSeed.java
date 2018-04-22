package seeds;

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
        
        List<WebElement> specTables = waitLong.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.className("model-information-table row-selection")));
        
        result.put(Fields.URL, driver.getCurrentUrl());
        result.put(Fields.FOUNDTIME, Long.toString(System.currentTimeMillis()));
        result.put(Fields.CATEGORY, Fields.DISPLAY);
        
        for (WebElement specTable : specTables) {
            List<WebElement> specRows = specTable.findElements(By.tagName("tr"));
            for (WebElement specRow : specRows) {
                List<WebElement> specValues = specRow.findElements(By.tagName("td"));
                String specName = specValues.get(0).getText();
                String specValue = specValues.get(1).getText();
                
                switch (StringUtils.lowerCase(specName)) {
                    case "brand": result.put(Fields.BRAND, specValue);
                    case "model": result.put(Fields.MODEL, specValue);
                    case "size class": result.put(Fields.SCREEN_SIZE, specValue);
                    case "resolution": result.put(Fields.RESOLUTION, specValue);
                    case "minimum response time": result.put(Fields.RESPONSE_TIME, specValue);
                    case "vertical frequency": result.put(Fields.REFRESH_RATE, specValue);
                    case "panel type": result.put(Fields.PANEL_TYPE, specValue);
                    case "connectivity": result.put(Fields.CONNECTORS, specValue);
                    case "vesa mount": 
                        String vesa1 = result.get(Fields.VESA_MOUNT);
                        if (vesa1 != null) {
                            result.put(Fields.VESA_MOUNT, vesa1 + " " + specValue);
                        } else {
                            result.put(Fields.VESA_MOUNT, specValue);
                        }
                    case "vesa interface": 
                        String vesa2 = result.get(Fields.VESA_MOUNT);
                        if (vesa2 != null) {
                            result.put(Fields.VESA_MOUNT, vesa2 + " " + specValue);
                        } else {
                            result.put(Fields.VESA_MOUNT, specValue);
                        }
                    case "aspect ratio": result.put(Fields.ASPECT_RATIO, specValue);
                    case "pixel pitch": result.put(Fields.PIXEL_PITCH, specValue);
                    case "pixel density": result.put(Fields.PIXEL_DENSITY, specValue);
                    case "brightness": result.put(Fields.BRIGHTNESS, specValue);
                    case "removable stand": result.put(Fields.REMOVABLE_STAND, specValue);
                    case "height adjustment": 
                        String height1 = result.get(Fields.HEIGHT_ADJUSTMENT);
                        if (height1 != null) {
                            result.put(Fields.HEIGHT_ADJUSTMENT,height1 + specValue);
                        } else {
                            result.put(Fields.HEIGHT_ADJUSTMENT, specValue);
                        }
                    case "height adjustment range":
                        String height2 = result.get(Fields.HEIGHT_ADJUSTMENT);
                        if (height2 != null) {
                            result.put(Fields.HEIGHT_ADJUSTMENT,height2 + specValue);
                        } else {
                            result.put(Fields.HEIGHT_ADJUSTMENT, specValue);
                        }
                    case "landscape/portrait pivot": result.put(Fields.PORTRAIT_PIVOT, specValue);
                    case "left/right swivel": result.put(Fields.SWIVEL_ADJUSTMENT, specValue);
                    case "forward/backward tilt": result.put(Fields.TILT_ADJUSTMENT, specValue);
                    case "forward tilt": result.put(Fields.FORWARD_TILT, specValue);
                    case "backward tilt": result.put(Fields.BACKWARD_TILT, specValue);
                    case "radius of curvature": result.put(Fields.CURVATURE, specValue);
                    case "display area": result.put(Fields.DISPLAY_AREA, specValue);
                    case "left swivel": result.put(Fields.LEFT_SWIVEL, specValue);
                    case "right swivel": result.put(Fields.RIGHT_SWIVEL, specValue);
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
