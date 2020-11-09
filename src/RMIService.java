import javax.crypto.SealedObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface RMIService extends Remote {
    public AuctionItem getSpec(int itemId, int clientId) throws RemoteException;
    public SealedObject getSpec(int itemId, SealedObject clientRequest) throws RemoteException;

    public HashMap<Integer, AuctionItem> getAuctionItems() throws RemoteException;
    public HashMap<Integer, AuctionItem> getClosedAuctionItems() throws RemoteException;
    public AuctionItem getAuctionItem(int id) throws RemoteException;
    public int createAuction(Seller seller, double startingPrice, String description, String name, double reserve) throws RemoteException;
    public boolean bidAuction(Bid bid) throws RemoteException;
    public void closeAuction(int itemId) throws RemoteException;
    public boolean indicateWinner(int itemId, int buyerId) throws RemoteException;
}
