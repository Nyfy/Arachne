package project.malachite.arachne;

public class ArachnePost {
    String title;
    String price;
    String description;
    String region;
    String address;
    String visits;
    
    public ArachnePost(String title, String price, String description, String region, String address, String visits) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.region = region;
        this.address = address;
        this.visits = visits;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getPrice() {
        return price;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getVisits() {
        return visits;
    }
    
    public void setVisits(String visits) {
        this.visits = visits;
    }
}
