package com.nathanial.auction;

import java.io.Serializable;

public class Bid implements Serializable {
    public Bid(double bidAmount, int buyerId, Integer itemId) {
        this.buyerId = buyerId;
        this.bidAmount = bidAmount;
        this.itemId = itemId;
    }

    private double bidAmount;
    private int buyerId;
    private Integer itemId;

    public int getBuyerId() {
        return buyerId;
    }
    public double getBidAmount() {
        return this.bidAmount;
    }
    public Integer getItemId() { return itemId; }
}
