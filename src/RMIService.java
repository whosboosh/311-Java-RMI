import javax.crypto.SealedObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface RMIService extends Remote {
    public AuctionItem getSpec(int itemId, int clientId) throws RemoteException;
    public SealedObject getSpec(int itemId, SealedObject clientRequest) throws RemoteException;

    public HashMap<Integer, AuctionItem> getAuctionItems() throws RemoteException;
    public HashMap<Integer, AuctionItem> getClosedAuctionItems() throws RemoteException;
    public Integer createAuction(Seller seller, double startingPrice, String description, String name, double reserve) throws RemoteException;
    public void bidAuction(Bid bid) throws RemoteException;
    public void closeAuction(Integer itemId) throws RemoteException;
    public Boolean indicateWinner(Integer itemId, Integer buyerId) throws RemoteException;
}
