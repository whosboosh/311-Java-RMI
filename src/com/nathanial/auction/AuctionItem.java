package com.nathanial.auction;

import java.io.Serializable;
import java.util.ArrayList;

public class AuctionItem implements Serializable {
    AuctionItem(String name, String description, double reserve, double startingPrice, int id, int sellerId) {
        this.name = name;
        this.description = description;
        this.reserveAmount = reserve;
        this.startingPrice = startingPrice;
        this.highestBidAmount = startingPrice;
        this.id = id;
        this.sellerId = sellerId;
    }
    private String description;
    private String name;
    private Integer id;
    private double reserveAmount;
    private double startingPrice;
    private ArrayList<Bid> currentBids = new ArrayList<>();
    private double highestBidAmount;
    private Integer winningBuyerId = null;
    private int sellerId;
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
    public void addBid(Bid bid) {
        currentBids.add(bid);
        highestBidAmount = bid.getBidAmount(); // Record the highest amount, if this function is called then its already been validated in com.nathanial.auction.ImplRMI bidAuction()
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
    public int getSellerId() { return sellerId; }
}
