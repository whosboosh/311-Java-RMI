import javax.crypto.Cipher;
import javax.crypto.SealedObject;

public class ImplRMI implements RMIService {
    private AuctionItem[] auctionItems = {new AuctionItem("Chair", "A chair to sit in", 0), new AuctionItem("Lamp", "An 19th Century Lamp", 1)};

    public AuctionItem getSpec(int itemId, int clientId) {
        return auctionItems[itemId];
    }
    public SealedObject getSpec(int itemId, SealedObject clientRequest) {
        SealedObject sealedResponse = null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipher.ENCRYPT_MODE, Utilities.getKey());
            sealedResponse = new SealedObject(auctionItems[itemId], cipher);
            System.out.println("Auction Item is now sealed, ready to sent to client");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sealedResponse;
    }
}
