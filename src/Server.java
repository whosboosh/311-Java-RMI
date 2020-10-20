import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server extends ImplRMI {
    private Server(){}

    public static void main(String[] args) {
        try {
            ImplRMI server = new ImplRMI();
            RMIService stub = (RMIService) UnicastRemoteObject.exportObject(server, 0);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ServerRMI", stub);
            System.out.println("Server Ready");
        } catch (RemoteException e) {
            System.err.println("Server exception "+e.toString());
            e.printStackTrace();
        }
    }
}