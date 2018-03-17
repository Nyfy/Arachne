package project.malachite.arachne;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebDriver;
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
    
    public String getShouldVisitRegex() {
        return shouldVisitRegex;
    }
    
    public String getTopic() {
        return topic;
    }
    
    //Need to override to process specific sources
    public String processResult(WebDriver driver) throws Exception {
        return null;
    }
}
