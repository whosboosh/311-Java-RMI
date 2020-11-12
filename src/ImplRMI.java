import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import java.rmi.RemoteException;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ImplRMI implements RMIService {
    private HashMap<Integer, AuctionItem> auctionItems = new HashMap<>();
    private HashMap<Integer, Seller> sellers = new HashMap<>();
    private HashMap<Integer, Buyer> buyers = new HashMap<>();

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public void generateKeys() {
        // Generate a symmetric key for the client and server
        KeyPair keyPair = Utilities.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void authoriseClient(Client client) {
        try {
            byte[] messageHash = Utilities.generateHash( "stringtoverify"+client.getId());

            byte[] serverResponse = client.challengeClient(messageHash); // Send the hash to the client, they encrypt it using their private key and return
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, client.getPublicKey());
            byte[] digitalSignature = cipher.doFinal(serverResponse);
            if (Arrays.equals(digitalSignature, messageHash)) {
                System.out.println("Client "+client.getId()+" is authorised");
            } else {
                System.out.println("Failed to authorise Client "+client.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] challengeServer(byte[] message) {
        return Utilities.performChallenge(privateKey, message);
    }

    public HashMap<Integer, AuctionItem> getAuctionItems() {
        return auctionItems;
    }

    public HashMap<Integer, Buyer> getBuyers() {
        return buyers;
    }

    public HashMap<Integer, Seller> getSellers() {
        return sellers;
    }

    public void addBuyer(Buyer buyer) {
        buyers.put(buyer.getId(), buyer);
    }

    public void addSeller(Seller seller) {
        sellers.put(seller.getId(), seller);
    }

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
            cipher.init(cipher.ENCRYPT_MODE, Utilities.getKey("key.txt"));
            sealedResponse = new SealedObject(auctionItems.get(itemId), cipher);
            System.out.println("Auction Item is now sealed, ready to sent to client");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sealedResponse;
    }

    // Return auction item based on id
    public AuctionItem getAuctionItem(int id) {
        return auctionItems.get(id);
    }

    public int createAuction(Seller seller, double startingPrice, String name, String description, double reserve) {
        int itemId = 0;
        for (int i = 0; i < auctionItems.size(); ++i) {
            if (auctionItems.get(i).getId() == i) itemId++;
            else break;
        }
        auctionItems.put(itemId, new AuctionItem(name, description, reserve, startingPrice, itemId, seller));
        System.out.println("Added item " + itemId);
        return itemId;
    }

    public double closeAuction(int itemId, Client client) {
        // Work out who the highest bidder is
        if (auctionItems.get(itemId) == null) return -1; // Item doesn't exist
        if (!auctionItems.get(itemId).getSeller().getId().equals(client.getId())) return -2; // Seller isn't authorised to close this auction
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
            return -4; // Failed to meet reserve
        } else {
            System.out.println(highestBid.getBuyer().getName()+" has won item "+auctionItems.get(itemId).getId());
            auctionItems.get(itemId).setWinningBuyerId(highestBid.getBuyer().getId()); // Mark winner as buyerId
            return 0;
        }
    }

    public synchronized double bidAuction(Bid bid) {
        // For an auction item with itemId, add bid to the currentBids HashMap
        AuctionItem item = auctionItems.get(bid.getItemId());
        if (item == null) return -1; // Make sure item id exists
        else if (item.isSold()) return -2; // If the item is already sold
        else if (bid.getBidAmount() <= item.getHighestBid()) return -3; // Check if bid is valid, highestBidAmount is always >= startingPrice
        item.addBid(bid);
        for (Bid i : item.getCurrentBids()) {
            System.out.println("Amount: "+i.getBidAmount() + " Buyer: "+i.getBuyer().getName() + " For item: " + i.getItemId());
        }
        return 0;
    }
}