import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

public class Buyer {

    public Buyer(String name, String email, Integer id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }
    private String name;
    private String email;
    private Integer id;

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public Integer getId() {
        return id;
    }

    public void main(String[] args) {
        try {
            // Find the registry
            Registry registry = LocateRegistry.getRegistry();
            RMIService stub = (RMIService) registry.lookup("ServerRMI"); // Create a stub based on the location of "ServerRMI" in the registry

            switch(args[0].toLowerCase()) {
                case "list":
                    HashMap<Integer, AuctionItem> auctionItems = stub.getAuctionItems();
                    for (AuctionItem item : auctionItems.values()) {
                        System.out.println("ID: " + "'"+item.getId()+"'" + " Name: " + "'"+item.getName()+"'" + " Description: " + "'"+item.getDescription()+"'");
                    }
                case "add":
                    // Bid on an auction item (args[2]), create bid with the bid amount (args[1]), pass this as a reference to who the buyer is
                    stub.bidAuction(new Bid(Double.parseDouble(args[1]), this), Integer.parseInt(args[2]));
                    break;
            }
        } catch(Exception e) {
            System.err.println("Client Exception "+e.toString());
            e.printStackTrace();
        }

    }
}
