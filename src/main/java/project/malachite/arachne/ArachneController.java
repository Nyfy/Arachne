package project.malachite.arachne;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

public class ArachneController {
    private static String BOOTSTRAP_SERVER = "localhost:9092";
    private static int THREAD_COUNT = 1;
    
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
        
        producer = new KafkaProducer<String, String>(producerProps);
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
    }
    
    public synchronized void sendResult(String result, String topic) {
        producer.send(new ProducerRecord<String, String>(topic,result));
    }
}
