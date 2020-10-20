import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private Client() {}
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry();
            RMIService stub = (RMIService) registry.lookup("ServerRMI");
            AuctionItem item = stub.getSpec(0, 1);
            System.out.println(item.getName());
            //stub.sendMsg("asddasd");
        } catch (Exception e) {
            System.err.println("Client Exception "+e.toString());
            e.printStackTrace();
        }
    }

}
