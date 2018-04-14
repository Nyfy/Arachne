package seeds;

import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Seed {
    private String shouldVisitRegex;
    private String shouldProcessRegex;
    private String topic;
    private List<String> seedUrls;
    
    public void setSeedUrls(List<String> seedUrls) {
        this.seedUrls = seedUrls;
    }
    
    public void setShouldProcessRegex(String shouldProcessRegex) {
        this.shouldProcessRegex = shouldProcessRegex;
    }
    
    public void setShouldVisitRegex(String shouldVisitRegex) {
        this.shouldVisitRegex = shouldVisitRegex;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public List<String> getSeedUrls() {
        return seedUrls;
    }
    
    public String getShouldProcessRegex() {
        return shouldProcessRegex;
    }
    
    public String getShouldVisitRegex() {
        return shouldVisitRegex;
    }
    
    public String getTopic() {
        return topic;
    }
    
    //Extend to process specific sources
    public String processResult(WebDriver driver, WebDriverWait waitShort, WebDriverWait waitLong) throws Exception {
        return null;
    }
}
