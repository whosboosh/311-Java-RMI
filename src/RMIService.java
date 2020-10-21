import javax.crypto.SealedObject;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIService extends Remote {
    public AuctionItem getSpec(int itemId, int clientId) throws RemoteException;
    public SealedObject getSpec(int itemId, SealedObject clientRequest) throws RemoteException;
}
