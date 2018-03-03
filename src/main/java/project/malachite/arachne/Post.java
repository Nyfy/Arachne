package project.malachite.arachne;

public class Post {
    String title;
    String price;
    String description;
    String address;
    String visits;
    String url;
    String foundTime;
    String category;
    
    public Post(String title, String price, String description, String address, 
            String visits, String url, String foundTime, String category) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.address = address;
        this.visits = visits;
        this.url = url;
        this.foundTime = foundTime;
        this.category = category;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFoundTime() {
        return foundTime;
    }

    public void setFoundTime(String foundTime) {
        this.foundTime = foundTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
