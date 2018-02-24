package project.malachite.arachne;

import org.jsoup.nodes.Document;

public class ArachneCrawlerKijiji extends ArachneCrawler {

    public ArachneCrawlerKijiji(String threadName, String shouldVisitRegex, String shouldProcessRegex,
            ArachneController controller) {
        super(threadName, shouldVisitRegex, shouldProcessRegex, controller);
    }

    @Override
    String processResult(Document doc) {
        return null;
    }
    

}
