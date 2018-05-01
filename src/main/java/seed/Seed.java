package seed;

import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class Seed {
    private String shouldVisitRegex;
    private String shouldProcessRegex;
    private String topic;
    private List<String> seedUrls;
    
    /**
     * Implement this function for each seed needed. 
     * The String returned is the value produced to the Kafka topic.
     */
    public abstract String processResult(WebDriver driver, WebDriverWait waitShort, WebDriverWait waitLong) throws Exception;
    
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
}
