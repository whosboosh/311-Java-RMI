import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public class ImplRMI implements RMIService {
    private HashMap<Integer, AuctionItem> auctionItems = new HashMap<>();
    private HashMap<Integer, AuctionItem> closedAuctions = new HashMap<>();

    // Return auction item with provided item Id
    public AuctionItem getSpec(int itemId, int clientId) {
        // Return the item out of auctionList with ID itemID
        return auctionItems.get(itemId);
    }

    // Return a sealed auction item using the symmetric key
    public SealedObject getSpec(int itemId, SealedObject clientRequest) {
        SealedObject sealedResponse = null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipher.ENCRYPT_MODE, Utilities.getKey());
            sealedResponse = new SealedObject(auctionItems.get(itemId), cipher);
            System.out.println("Auction Item is now sealed, ready to sent to client");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sealedResponse;
    }

    public HashMap<Integer, AuctionItem> getAuctionItems() { return auctionItems; }
    public HashMap<Integer, AuctionItem> getClosedAuctionItems() { return closedAuctions; }
    public AuctionItem getAuctionItem(int id) { return  auctionItems.get(id); }

    public int createAuction(Seller seller, double startingPrice, String name, String description, double reserve) {
        int itemId = 0;
        for (int i = 0; i < auctionItems.size(); ++i) {
            if (auctionItems.get(i).getId() == i) itemId++;
            else break;
        }
        auctionItems.put(itemId, new AuctionItem(name, description, reserve, startingPrice, itemId, seller));
        System.out.println("Added item "+itemId);
        return itemId;
    }

    public void closeAuction(int itemId) {
        // Work out who the highest bidder is
        Bid highestBid = auctionItems.get(itemId).getCurrentBids().get(0);
        for (Bid bid : auctionItems.get(itemId).getCurrentBids()) {
            // Get the highest bid
            if (bid.getBidAmount() > highestBid.getBidAmount()) {
                highestBid = bid;
            }
            // Logic for if two bids have the same price (choose first bidder?)
        }
        System.out.println(highestBid.getBuyer().getName());

        auctionItems.get(itemId).setWinningBuyerId(highestBid.getBuyer().getId()); // Mark winner as buyerId
        closedAuctions.put(itemId, auctionItems.get(itemId)); // Move the item into the "closed auctions array"
        auctionItems.remove(itemId); // Remove item from auctionItems as it's been sold
    }

    public boolean bidAuction(Bid bid) {
        // For an auction item with itemId, add bid to the currentBids HashMap
        return auctionItems.get(bid.getItemId()).addBid(bid);
    }

    public boolean indicateWinner(int itemId, int buyerId) {
        // Return true if the item in closedAuctions winning id matches the buyer id
        if (closedAuctions.get(itemId) == null) return false; //  if the item doesn't exist
        return closedAuctions.get(itemId).getWinningBuyerId().equals(buyerId);
    }
}