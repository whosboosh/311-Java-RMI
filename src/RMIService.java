import javax.crypto.SealedObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface RMIService<Arraylist> extends Remote {
    public AuctionItem getSpec(int itemId, int clientId) throws RemoteException;
    public SealedObject getSpec(int itemId, SealedObject clientRequest) throws RemoteException;

    public HashMap<Integer, AuctionItem> getAuctionItems() throws RemoteException;
    public Integer createAuction(double startingPrice, String description, String name, double reserve) throws RemoteException;
    public void bidAuction(Bid bid, Integer itemId) throws RemoteException;
}
