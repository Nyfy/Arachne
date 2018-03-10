package project.malachite.arachne;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Seedling {
    private List<Seed> seeds = new ArrayList<Seed>();
    
    private static String CATEGORY = "Category";
    
    private static String MONITOR = "Monitor";
    
    private static String BRAND = "Brand";
    private static String PRICE = "Price";
    private static String MODEL = "ModelNumber";
    private static String SCREEN_SIZE = "ScreenSize";
    private static String RESOLUTION = "Resolution";
    private static String RESPONSE_TIME = "ResponseTime";
    private static String REFRESH_RATE = "RefreshRate";
    private static String PANEL_TYPE = "PanelType";
    private static String ADAPTIVE_SYNC = "AdaptiveSync";
    private static String VGA = "VGA";
    private static String DVI = "DVI";
    private static String HDMI = "HDMI";
    private static String DISPLAY_PORT = "DisplayPort";
    private static String VESA = "VesaMount";
    private static String WEIGHT = "Weight";
    private static String RATING = "Rating";
    
    public Seedling() {
        createNeweggMonitorSeed();
    }
    
    public boolean hasAvailableSeed() {
        return !seeds.isEmpty();
    }
    
    public Seed getNextSeed() {
        return seeds.remove(0);
    }
    
    private void createNeweggMonitorSeed() {
        List<String> neweggMonitorSeeds = new ArrayList<String>();
        neweggMonitorSeeds.add("https://www.newegg.ca/LCD-LED-Monitors/SubCategory/ID-20");
        
        List<String> neweggMonitorVisit = new ArrayList<String>();
        neweggMonitorVisit.add("\\/LCD-LED-Monitors\\/SubCategory\\/ID-20\\/Page-");
        
        String neweggMonitorsProcess = "\\/Product\\/Product.aspx\\?Item=";
        String neweggMonitorsTopic = "Monitors-Raw";
        
        seeds.add(new Seed(neweggMonitorSeeds, neweggMonitorVisit, neweggMonitorsProcess, neweggMonitorsTopic) {
            @Override
            public String processResult(WebDriver driver) throws Exception {
                boolean isMonitor = false;
                for (WebElement link : driver.findElement(By.className("breadcrumb")).findElements(By.tagName("a"))) {
                    if (StringUtils.containsIgnoreCase(link.getText(),"LCD / LED Monitors")) {
                        isMonitor = true;
                    }
                }
                
                if (isMonitor) {
                    Map<String,String> result = new HashMap<String,String>();
                    ObjectMapper objectMapper = new ObjectMapper();
                    
                    driver.findElement(By.id("Details_Tab")).click();
                    
                    result.put(CATEGORY, MONITOR);
                    try {
                        result.put(PRICE, driver.findElement(By.className("price-current")).getText());
                    } catch (NoSuchElementException e) {
                        e.getLocalizedMessage();
                    }
                    try {
                        result.put(RATING, driver.findElement(By.className("itmRating")).findElement(By.tagName("i")).getAttribute("title"));
                    } catch (NoSuchElementException e) {
                        e.getLocalizedMessage();
                    }
                    List<WebElement> specs = driver.findElement(By.id("Specs")).findElements(By.tagName("dl"));
                    for (WebElement field : specs) {
                        WebElement fieldTitleElement = field.findElement(By.tagName("dt"));
                        String fieldTitle = fieldTitleElement.getText();
                        if (StringUtils.containsIgnoreCase(fieldTitle, "brand")) {
                            result.put(BRAND, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "model")) {
                            result.put(MODEL, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "screen size")) {
                            result.put(SCREEN_SIZE, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "maximum resolution")) {
                            result.put(RESOLUTION, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "recommended resolution") && !result.containsKey(RESOLUTION)) {
                            result.put(RESOLUTION, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "resolution") && !result.containsKey(RESOLUTION)) {
                            result.put(RESOLUTION, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "response time")) {
                            result.put(RESPONSE_TIME, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "vertical refresh rate")) {
                            result.put(REFRESH_RATE, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "refresh rate") && !result.containsKey(REFRESH_RATE)) {
                            result.put(REFRESH_RATE, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "panel")) {
                            result.put(PANEL_TYPE, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "adaptive sync")) {
                            result.put(ADAPTIVE_SYNC, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "d-sub")) {
                            result.put(VGA, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "vga")) {
                            result.put(VGA, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "dvi")) {
                            result.put(DVI, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "hdmi")) {
                            result.put(HDMI, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "displayport")) {
                            result.put(DISPLAY_PORT, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "vesa")) {
                            result.put(VESA, field.findElement(By.tagName("dd")).getText());
                        } else if (StringUtils.containsIgnoreCase(fieldTitle, "weight")) {
                            result.put(WEIGHT, field.findElement(By.tagName("dd")).getText());
                        }
                    }
                    return objectMapper.writeValueAsString(result);
                }
                return null;
            }
        });
    }
}
