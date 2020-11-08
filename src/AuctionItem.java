import java.io.Serializable;
import java.util.ArrayList;

public class AuctionItem implements Serializable {
    AuctionItem(String name, String description, double reserve, double startingPrice, Integer id) {
        this.name = name;
        this.description = description;
        this.reserveAmount = reserve;
        this.startingPrice = startingPrice;
        this.id = id;
    }
    private String description;
    private String name;
    private Integer id;
    private double reserveAmount;
    private double startingPrice;
    private ArrayList<Bid> currentBids;


    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public Integer getId() {
        return id;
    }
    public double getReserve() { return reserveAmount; }
    public double getStartingPrice() { return startingPrice; }
    public ArrayList<Bid> getCurrentBids(){ return currentBids; }
    public void addBid(Bid bid) {
        currentBids.add(bid);
    }
}
