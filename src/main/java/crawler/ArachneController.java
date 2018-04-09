package crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import constant.Fields;
import constant.Config;
import seed.NeweggSeed;
import seed.Seed;

public class ArachneController {
    private static int THREAD_COUNT = 1;
    
    private static Logger logger = Logger.getLogger(ArachneController.class);
    private KafkaProducer<String, String> producer;
    
    private List<Seed> seeds = new ArrayList<Seed>();
    
    public static void main(String[] args) throws IOException {
        ArachneController controller = new ArachneController();
        controller.run();
    }
    
    public ArachneController() {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BOOTSTRAP_SERVER);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");
        
        initializeSeeds();
        
        producer = new KafkaProducer<String, String>(producerProps);
        
        logger.info("Initialized Producer sending to "+Config.BOOTSTRAP_SERVER);
    }
    
    private void initializeSeeds() {
        seeds.add(new NeweggSeed());
    }
    
    protected void run() {
        List<ArachneCrawler> crawlers = new ArrayList<ArachneCrawler>();
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            crawlers.add(new ArachneCrawler(this));
        }
        
        for (ArachneCrawler crawler : crawlers) {
            if (CollectionUtils.isNotEmpty(seeds)) {
                crawler.addSeed(seeds.remove(0));
            }
        }
        
        for (ArachneCrawler crawler : crawlers) {
            crawler.start();
        }
        
        logger.info("Initialized and started "+THREAD_COUNT+" crawlers.");
    }
    
    public synchronized void sendResult(String result, String topic) throws InterruptedException, ExecutionException {
        logger.info(producer.send(new ProducerRecord<String, String>(topic,result)).get().toString());
        logger.info("Sent \""+result+"\" to "+topic);
    }
}
