import java.io.Serializable;
import java.util.ArrayList;

public class AuctionItem implements Serializable {
    AuctionItem(String name, String description, double reserve, double startingPrice, Integer id, Seller seller) {
        this.name = name;
        this.description = description;
        this.reserveAmount = reserve;
        this.startingPrice = startingPrice;
        this.id = id;
        this.seller = seller;
    }
    private String description;
    private String name;
    private Integer id;
    private double reserveAmount;
    private double startingPrice;
    private ArrayList<Bid> currentBids = new ArrayList<>();
    private Integer winningBuyerId;
    private Seller seller;

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
        for (Bid item : currentBids) {
            System.out.println("Amount: "+item.getBidAmount() + " Buyer: "+item.getBuyer().getName() + " For item: " + item.getItemId());
        }
    }
    public void setWinningBuyerId(Integer buyerId) {
        winningBuyerId = buyerId;
    }
    public Integer getWinningBuyerId() {
        return winningBuyerId;
    }
    public Seller getSeller() { return seller; }
}
