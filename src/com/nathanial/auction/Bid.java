package com.nathanial.auction;

import java.io.Serializable;

public class Bid implements Serializable {
    public Bid(double bidAmount, Buyer buyer, Integer itemId) {
        this.buyer = buyer;
        this.bidAmount = bidAmount;
        this.itemId = itemId;
    }

    private double bidAmount;
    private Buyer buyer;
    private Integer itemId;

    public Buyer getBuyer() {
        return buyer;
    }
    public double getBidAmount() {
        return this.bidAmount;
    }
    public Integer getItemId() { return itemId; }
}
