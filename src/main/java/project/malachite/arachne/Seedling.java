package project.malachite.arachne;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Seedling {
    private List<Seed> seeds = new ArrayList<Seed>();
    
    public Seedling() {
        createSeeds();
    }
    
    public boolean hasAvailableSeed() {
        return !seeds.isEmpty();
    }
    
    public Seed getNextSeed() {
        return seeds.remove(0);
    }
    
    private void createSeeds() {
        List<String> kijijiMonitors = new ArrayList<String>();
        kijijiMonitors.add("https://www.kijiji.ca/b-monitors/ottawa/page-1/c782l1700185");
        kijijiMonitors.add("https://www.kijiji.ca/b-monitors/ottawa/page-2/c782l1700185");
        kijijiMonitors.add("https://www.kijiji.ca/b-monitors/ottawa/page-3/c782l1700185");
        kijijiMonitors.add("https://www.kijiji.ca/b-monitors/ottawa/page-4/c782l1700185");
        kijijiMonitors.add("https://www.kijiji.ca/b-monitors/ottawa/page-5/c782l1700185");
        
        String kijijiMonitorsProcess = "v.monit.{4,5}ottawa";
        String kijijiMonitorsVisit = "v.monit.{4,5}ottawa";
        
        String kijijiMonitorsTopic = "Monitors-Raw";
        
        seeds.add(new Seed(kijijiMonitors, kijijiMonitorsVisit, kijijiMonitorsProcess, kijijiMonitorsTopic) {
            @Override
            public String processResult(String url, WebDriver driver) throws JsonProcessingException {
                String title = null;
                String price = null;
                String description = null;
                String address = null;
                String visits = null;
                String foundTime = null;
                String category = null;
                
                ObjectMapper objectMapper = new ObjectMapper();
                
                title = driver.findElement(By.className("title-3283765216")).getText();
                price = driver.findElement(By.className("currentPrice-2872355490")).getText();
                description = driver.findElement(By.className("descriptionContainer-2832520341")).getText();
                address = driver.findElement(By.className("address-2932131783")).getText();
                visits = driver.findElement(By.className("visitCounter-450272408")).getText();
                foundTime = Long.toString(System.currentTimeMillis());
                category = "Monitors";
                   
                return objectMapper.writeValueAsString
                        (new Post(title,price,description,address,visits, url, foundTime, category));
            }
        });
    }
}
