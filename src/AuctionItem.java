import java.io.Serializable;
import java.util.ArrayList;

public class AuctionItem implements Serializable {
    AuctionItem(String name, String description, double reserve, double startingPrice, Integer id, Seller seller) {
        this.name = name;
        this.description = description;
        this.reserveAmount = reserve;
        this.startingPrice = startingPrice;
        this.highestBidAmount = startingPrice;
        this.id = id;
        this.seller = seller;
    }
    private String description;
    private String name;
    private Integer id;
    private double reserveAmount;
    private double startingPrice;
    private ArrayList<Bid> currentBids = new ArrayList<>();
    private double highestBidAmount;
    private Integer winningBuyerId = null;
    private Seller seller;
    private boolean sold = false;

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
    // Method is synchronized because we don't want more than 1 thread entering function at at time
    public synchronized double addBid(Bid bid) {
        // Check if bid is valid, highestBidAmount is always >= startingPrice
        if (sold) return -1;
        else if (bid.getBidAmount() <= highestBidAmount) return -2;
        currentBids.add(bid);
        highestBidAmount = bid.getBidAmount();
        for (Bid item : currentBids) {
            System.out.println("Amount: "+item.getBidAmount() + " Buyer: "+item.getBuyer().getName() + " For item: " + item.getItemId());
        }
        return 0;
    }
    public void setSold(boolean isSold) { this.sold = isSold; }
    public boolean isSold() { return sold; }
    public double getHighestBid() {
        return highestBidAmount;
    }
    public void setWinningBuyerId(Integer buyerId) {
        winningBuyerId = buyerId;
    }
    public Integer getWinningBuyerId() {
        return winningBuyerId;
    }
    public Seller getSeller() { return seller; }
}
