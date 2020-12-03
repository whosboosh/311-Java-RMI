package com.nathanial.auction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerData implements Serializable {
    public ServerData(HashMap<Integer, AuctionItem> auctionItems, ArrayList<Integer> buyers, ArrayList<Integer> sellers) {
        this.auctionItems = auctionItems;
        this.sellers = sellers;
        this.buyers = buyers;
    }
    public ServerData() {

    }
    public void setValues(HashMap<Integer, AuctionItem> auctionItems, ArrayList<Integer> buyers, ArrayList<Integer> sellers) {
        this.auctionItems = auctionItems;
        this.sellers = sellers;
        this.buyers = buyers;
    }

    private HashMap<Integer, AuctionItem> auctionItems = new HashMap<>();
    private ArrayList<Integer> sellers = new ArrayList<>();
    private ArrayList<Integer> buyers = new ArrayList<>();

    public HashMap<Integer, AuctionItem> getAuctionItems() { return auctionItems; }
    public ArrayList<Integer> getSellers() { return sellers; }
    public ArrayList<Integer> getBuyers() { return buyers; }
}
