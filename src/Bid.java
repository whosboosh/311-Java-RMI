public class Bid {
    public Bid(double bidAmount, Buyer buyer) {
        this.buyer = buyer;
        this.bidAmount = bidAmount;
    }

    private double bidAmount;
    private Buyer buyer;

    public Buyer getBuyer() {
        return buyer;
    }
    public double getBidAmount() {
        return this.bidAmount;
    }
}
