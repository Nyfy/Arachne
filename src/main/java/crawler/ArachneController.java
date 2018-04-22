package crawler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import config.KafkaConfig;
import seeds.*;

public class ArachneController {
    private Properties arachneProps;
    private static String arachnePropsFile = "arachne.properties";
    private static String THREAD_COUNT = "thread.count";
    private static String PRODUCER_ACKS = "producer.acks";
    private static String PRODUCER_RETRIES = "producer.retries";
    private static String ASSIGNED_SEEDS = "seeds.include";
    private static String SEED_SEPARATOR = ",";
    
    private static Logger logger = Logger.getLogger(ArachneController.class);
    private static KafkaProducer<String, String> producer;
    private static List<Seed> seeds = new ArrayList<Seed>();
    private static List<ArachneCrawler> crawlers;
    
    public ArachneController() {
        initializeProperties();
        initializeProducer();
        initializeSeeds();
    }
    
    public static void main(String[] args) throws IOException {
        ArachneController controller = new ArachneController();
        controller.run();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                producer.close();
            }
        });
    }
    
    private void run() {
        crawlers = new ArrayList<ArachneCrawler>();
        try {
            int threadCount = Integer.parseInt(arachneProps.getProperty(THREAD_COUNT));
            
            for (int i = 0; i < threadCount; i++) {
                crawlers.add(new ArachneCrawler(this, true));
            }
            while (CollectionUtils.isNotEmpty(seeds)){
                for (ArachneCrawler crawler : crawlers) {
                    if (CollectionUtils.isNotEmpty(seeds)) {
                        crawler.addSeed(seeds.remove(0));
                    }
                }
            }
            
            for (ArachneCrawler crawler : crawlers) {
                crawler.start();
            }
            
            logger.info("Initialized and started "+threadCount+" crawlers.");
        } catch (Exception e) {
            logger.error("An unexpected error occured while running crawlers.",e);
        }
    }
    
    private void initializeProperties() {
        arachneProps = new Properties();
        InputStream input;
        try {
            input = ArachneController.class.getClassLoader().getResourceAsStream(arachnePropsFile);
            arachneProps.load(input);
            if (input != null) {
                input.close();
            }
        } catch (FileNotFoundException e) {
            logger.error("Unable to load arachne properties file.",e);
        } catch (Exception e) {
            logger.error("Unexpected error occured while loading arachne properties.", e);;
        }
    }
    
    private void initializeProducer() {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVER);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.ACKS_CONFIG, arachneProps.getProperty(PRODUCER_ACKS));
        producerProps.put(ProducerConfig.RETRIES_CONFIG, arachneProps.getProperty(PRODUCER_RETRIES));
        
        producer = new KafkaProducer<String, String>(producerProps);
        logger.info("Initialized Producer sending to "+KafkaConfig.BOOTSTRAP_SERVER);
    }
    
    private void initializeSeeds() {
        Class<?> seedClass;
        String[] assignedSeeds = arachneProps.getProperty(ASSIGNED_SEEDS).split(SEED_SEPARATOR);
        
        for (String seed : assignedSeeds) {
            try {
                seedClass = Class.forName("seeds."+seed);
                seeds.add((Seed) seedClass.newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                logger.error("An error occured while initializing seed: "+seed, e);;
            }
        }
    }
    
    public synchronized void sendResult(String result, String topic) {
        producer.send(new ProducerRecord<String, String>(topic,result));
        logger.info("Sent \""+result+"\" to "+topic);
    }
}
