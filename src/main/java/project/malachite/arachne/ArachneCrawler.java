package project.malachite.arachne;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public abstract class ArachneCrawler extends Thread {
    
    private String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.78 Safari/537.36";
    
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
                try {
                    Connection connection = Jsoup.connect(url);
                    connection.userAgent(USER_AGENT);
                    Document doc = connection.get();
                    
                    for (Element newLink : doc.select("a[href]")) {
                        String newUrl = newLink.absUrl("href");
                        
                        Matcher shouldVisit = Pattern.compile(shouldVisitRegex).matcher(newUrl);
                        
                        if (shouldVisit.find()) {
                            controller.addUrl(newUrl);
                        }
                    }
                    
                    Matcher shouldProcess = Pattern.compile(shouldProcessRegex).matcher(url);
                    
                    if (shouldProcess.find()) {
                        String jsonResult = processResult(doc);
                    }
                    
                    controller.sendResult(jsonResult);
                    
                } catch (IOException e) {
                    System.out.println("Thread "+threadName+" failed to reach "+url);
                    controller.addUrl(url);
                }    
            }
        }
    }
    
    //Implement to process scraped documents into desired string
    abstract String processResult(Document doc);
}
