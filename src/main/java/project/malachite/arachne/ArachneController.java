package project.malachite.arachne;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

public class ArachneController {
    private static String BOOTSTRAP_SERVER = "192.168.0.20:9092";
    private static int THREAD_COUNT = 1;
    
    private static Logger logger = Logger.getLogger(ArachneController.class);
    private KafkaProducer<String, String> producer;
    
    public static void main(String[] args) throws IOException {
        ArachneController controller = new ArachneController();
        controller.run();
    }
    
    public ArachneController() {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");
        
        producer = new KafkaProducer<String, String>(producerProps);
        
        logger.info("Initialized Producer sending to "+BOOTSTRAP_SERVER);
    }
    
    protected void run() {
        List<ArachneCrawler> crawlers = new ArrayList<ArachneCrawler>();
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            crawlers.add(new ArachneCrawler(this));
        }
        
        Seedling seedling = new Seedling();
        
        while (seedling.hasAvailableSeed()) {
            for (ArachneCrawler crawler : crawlers) {
                if (seedling.hasAvailableSeed()) {
                    crawler.addSeed(seedling.getNextSeed());
                }
            }
        }
        
        for (ArachneCrawler crawler : crawlers) {
            crawler.start();
        }
        
        logger.info("Initialized and started "+THREAD_COUNT+" crawlers.");
    }
    
    public void sendResult(String result, String topic) {
        //producer.send(new ProducerRecord<String, String>(topic,result));
        logger.info("Sent \""+result+"\" to "+topic);
    }
}
