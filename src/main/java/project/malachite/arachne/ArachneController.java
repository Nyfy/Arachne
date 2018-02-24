package project.malachite.arachne;

import java.io.FileInputStream;
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

public abstract class ArachneController {
    
    private KafkaProducer<String, String> producer;
    private String topic;
    
    private int threadCount;
    
    private long seedInterval;
    private long lastSeedTime;
    private String[] seeds;
    
    private String shouldVisitRegex;
    private String shouldProcessRegex;
    private Set<String> pagesVisited = new HashSet<String>();
    private List<String> pagesToVisit = new LinkedList<String>();
    
    public ArachneController(Properties props) {
        topic = props.getProperty("topic");
        threadCount = Integer.parseInt(props.getProperty("thread.count"));
        
        shouldVisitRegex = props.getProperty("should.visit.url");
        shouldProcessRegex = props.getProperty("should.process.url");
        seeds = props.getProperty("seeds").split(",");
        seedInterval = Long.parseLong(props.getProperty("seed.interval.minutes"));
        
        lastSeedTime = System.currentTimeMillis();
        seed();
        
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
            crawlers.add(createCrawler("crawler"+Integer.toString(i), shouldVisitRegex, shouldProcessRegex, this));
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
        while (true) {
            if (CollectionUtils.isNotEmpty(pagesToVisit)) {
                ArachneCrawler lowestLoad = crawlers.get(0);
                for (ArachneCrawler crawler : crawlers) {
                    if (crawler.getLoad() < lowestLoad.getLoad()) {
                        lowestLoad = crawler;
                    }
                }
                lowestLoad.addNextUrl(nextUrl());
            } else if ((System.currentTimeMillis() - lastSeedTime) > seedInterval*60000) {
                seed();
            }
        }
    }
    
    private void seed() {
        for (String seed : seeds) {
            pagesToVisit.add(seed);
        }
    }
    
    private String nextUrl()
    {
        String nextUrl;
        do
        {
            nextUrl = this.pagesToVisit.remove(0);
        } while(this.pagesVisited.contains(nextUrl) && CollectionUtils.isNotEmpty(pagesToVisit));
        this.pagesVisited.add(nextUrl);
        return nextUrl;
    }
    
    public synchronized void addUrl(String url) {
        pagesToVisit.add(url);
    }
    
    public synchronized void sendResult(String result) {
        producer.send(new ProducerRecord<String, String>(topic,result));
    }
    
    //Implement to create an instance of your custom crawler
    abstract ArachneCrawler createCrawler(String name, String shouldVisit, String shouldProcess, ArachneController controller);
}
