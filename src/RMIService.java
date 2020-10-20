import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIService extends Remote {
    public AuctionItem getSpec(int itemId, int clientId) throws RemoteException;
    public void sendMsg(String string) throws RemoteException;
}
