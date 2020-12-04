package com.nathanial.auction;
import java.util.ArrayList;
import java.util.HashMap;
import org.jgroups.ReceiverAdapter;

public abstract class AuctionImpl extends ReceiverAdapter implements JgroupsService {
    protected HashMap<Integer, AuctionItem> auctionItems = new HashMap<>();
    protected ArrayList<Integer> sellers = new ArrayList<>();
    protected ArrayList<Integer> buyers = new ArrayList<>();

    public void removeSeller(int id) {
        sellers.remove(id);
    }

    public void removeBuyer(int id) {
        buyers.remove(id);
    }

    public ArrayList<Integer> getSellers() {
        return sellers;
    }

    public ArrayList<Integer> getBuyers() {
        return buyers;
    }

    public void addBuyer(int buyerId) {
        buyers.add(buyerId);
    }

    public void addSeller(int sellerId) {
        sellers.add(sellerId);
    }

    // Return auction item based on id
    public AuctionItem getAuctionItem(int id) {
        return auctionItems.get(id);
    }
    public HashMap<Integer, AuctionItem> getAuctionItems() {
        return auctionItems;
    }

    public int createAuction(int sellerId, double startingPrice, String name, String description, double reserve) {
        // Generate ID for auction ID, just a sequentially generated ID based on the number of auction items
        int itemId = 0;
        for (int i = 0; i < auctionItems.size(); ++i) {
            if (auctionItems.get(i).getId() == i) itemId++;
            else break;
        }
        auctionItems.put(itemId, new AuctionItem(name, description, reserve, startingPrice, itemId, sellerId));
        System.out.println("Added item " + itemId);
        return itemId;
    }

    public double closeAuction(int itemId, int clientId) {
        // Work out who the highest bidder is
        if (auctionItems.get(itemId) == null) return -1; // Item doesn't exist
        if (auctionItems.get(itemId).getSellerId() != clientId) return -2; // Seller isn't authorised to close this auction
        if (auctionItems.get(itemId).getCurrentBids().isEmpty()) return -3; // No bids on item
        Bid highestBid = auctionItems.get(itemId).getCurrentBids().get(0);
        for (Bid bid : auctionItems.get(itemId).getCurrentBids()) {
            // Get the highest bid
            if (bid.getBidAmount() > highestBid.getBidAmount()) {
                highestBid = bid;
            }
            // Logic for if two bids have the same price (choose first bidder?)
        }
        auctionItems.get(itemId).setSold(true); // Flag as sold, if reserve wasn't met no winner is set but still closes the auction
        if (highestBid.getBidAmount() <= auctionItems.get(itemId).getReserve()) {
            System.out.println("Reserve was not met for item "+itemId);
            return -4; // Failed to meet reserve
        } else {
            System.out.println("Buyer: "+highestBid.getBuyerId()+" has won item with ID: "+itemId+" "+ auctionItems.get(itemId).getName());
            auctionItems.get(itemId).setWinningBuyerId(highestBid.getBuyerId()); // Mark winner as buyerId
            return 0;
        }
    }

    // Method is synchronized to prevent two buyers to place bids at the same time and allow both of them to be processed if highest bid hasn't been set yet
    public synchronized double bidAuction(Bid bid) {
        // For an auction item with itemId, add bid to the currentBids HashMap
        AuctionItem item = auctionItems.get(bid.getItemId());
        if (item == null) return -1; // Make sure item id exists
        else if (item.isSold()) return -2; // If the item is already sold
        else if (bid.getBidAmount() <= item.getHighestBid()) return -3; // Check if bid is valid, highestBidAmount is always >= startingPrice
        item.addBid(bid);
        for (Bid i : item.getCurrentBids()) {
            System.out.println("Amount: "+i.getBidAmount() + " Buyer: "+i.getBuyerId() + " For item: " + i.getItemId());
        }
        return 0;
    }
}