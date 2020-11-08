import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public class ImplRMI implements RMIService {
    //private ArrayList<AuctionItem> auctionItems;
    private HashMap<Integer, AuctionItem> auctionItems = new HashMap<Integer, AuctionItem>();

    public AuctionItem getSpec(int itemId, int clientId) {
        // Return the item out of auctionList with ID itemID
        return auctionItems.get(itemId);
    }
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

    public Integer createAuction(double startingPrice, String name, String description, double reserve) {
        Integer itemId = 0;
        for (int i = 0; i < auctionItems.size(); ++i) {
            if (auctionItems.get(i).getId() == i) itemId++;
            else break;
        }
        auctionItems.put(itemId, new AuctionItem(name, description, reserve, startingPrice, itemId));

        return itemId;
    }

     public void closeAuction(Integer itemId) {
        auctionItems.get(itemId);
     }

     public void bidAuction(Bid bid, Integer itemId) {
        // For an auction item with itemId, add bid to the currentBids ArrayList
         auctionItems.get(itemId).addBid(bid);
     }

     public Boolean indicateWinner(Integer itemId, int clientId) {
        // return true if auction item no longer exists in auctionItems and clientId is the winning client
         return false;
     }
}