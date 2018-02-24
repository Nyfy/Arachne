package project.malachite.arachne;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ArachneControllerKijiji extends ArachneController {
    
    public static void main(String[] args) {
        Properties props = new Properties();
        InputStream input = null;
        
        try {
            input = ArachneController.class.getResourceAsStream("config.properties");
            props.load(input);
            
            ArachneControllerKijiji controller = new ArachneControllerKijiji(props);
            controller.run();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public ArachneControllerKijiji(Properties props) {
        super(props);
    }
    
    protected ArachneCrawler createCrawler(String name, String shouldVisit, String shouldProcess, ArachneController controller) {
        return new ArachneCrawlerKijiji(name,shouldVisit,shouldProcess,controller);
    }

}
