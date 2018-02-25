package project.malachite.arachne;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class ArachneController {
    
    private KafkaProducer<String, String> producer;
    private String topic;
    
    private int threadCount;
    
    private String[] seeds;
    
    private String shouldVisitRegex;
    private String shouldProcessRegex;
    private Set<String> pagesVisited = new HashSet<String>();
    private List<String> pagesToVisit = new LinkedList<String>();
    
    public static void main(String[] args) {
        Properties props = new Properties();
        InputStream input = null;
        
        try {
            input = ArachneController.class.getResourceAsStream("config.properties");
            props.load(input);
            
            ArachneController controller = new ArachneController(props);
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
    
    public ArachneController(Properties props) {
        topic = props.getProperty("topic");
        threadCount = Integer.parseInt(props.getProperty("thread.count"));
        
        shouldVisitRegex = props.getProperty("should.visit.url");
        shouldProcessRegex = props.getProperty("should.process.url");
        seeds = props.getProperty("seeds").split(",");
        
        for (String seed : seeds) {
            pagesToVisit.add(seed);
        }
        
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", props.get("bootstrap.server"));
        producerProps.put("linger.ms", 1);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        
        producer = new KafkaProducer<String, String>(producerProps);
    }
    
    protected void run() {
        List<ArachneCrawler> crawlers = new ArrayList<ArachneCrawler>();
        for (int i = 0; i < threadCount; i++) {
            crawlers.add(new ArachneCrawler("crawler"+Integer.toString(i),shouldVisitRegex,shouldProcessRegex,this));
        }
        
        while (CollectionUtils.isNotEmpty(pagesToVisit)) {
            for (ArachneCrawler crawler : crawlers) {
                if (CollectionUtils.isNotEmpty(pagesToVisit)) {
                    crawler.addNextUrl(nextUrl());
                }
            }
        }
        for (ArachneCrawler crawler : crawlers) {
            crawler.start();
        }
    }
    
    private String nextUrl()
    {
        String nextUrl;
        do
        {
            nextUrl = this.pagesToVisit.remove(0);
        } while(this.pagesVisited.contains(nextUrl));
        this.pagesVisited.add(nextUrl);
        return nextUrl;
    }
    
    public void sendResult(String result) {
        producer.send(new ProducerRecord<String, String>(topic,result));
    }
}
