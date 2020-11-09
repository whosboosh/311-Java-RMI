import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class ClientBuyer {
    public static void main(String[] args) {
        try {
            // Find the registry
            Registry registry = LocateRegistry.getRegistry();
            RMIService stub = (RMIService) registry.lookup("ServerRMI"); // Create a stub based on the location of "ServerRMI" in the registry

            HashMap<Integer, Buyer> buyers = new HashMap<>();
            Buyer currentBuyer = null;

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String[] splitted = input.split("\\s+"); // Split by each word
                System.out.println(Arrays.toString(splitted));
                switch(splitted[0].toLowerCase()) {
                    case "auth":
                        switch(splitted[1].toLowerCase()) {
                            case "login":
                                if (buyers.isEmpty()) {
                                    System.out.println("No accounts found, create one with 'auth create'");
                                    break;
                                }
                                // Needs 3 commands to login, check if less than 3 and if true break
                                if (splitted.length < 3) {
                                    System.out.println("Please provide an id to login as");
                                    break;
                                } else {
                                    Buyer tryBuyer = buyers.get(Integer.parseInt(splitted[2]));
                                    if (tryBuyer == null) {
                                        System.out.println("No seller with ID found");
                                        break;
                                    } else {
                                        currentBuyer = tryBuyer;
                                        System.out.println("Now logged in as: " + tryBuyer.getId());
                                    }
                                }
                                break;
                            case "create":
                                if (splitted.length < 4) {
                                    System.out.println("Please create a buyer account using the syntax: `auth create <name> <email>`");
                                    break;
                                }
                                // Create a new buyer with unique id
                                Integer buyerId = 0;
                                for (int i = 0; i < buyers.size(); ++i) {
                                    if (buyers.get(i).getId() == i) buyerId++;
                                    else break;
                                }
                                currentBuyer = new Buyer(splitted[2], splitted[3], buyerId);
                                buyers.put(buyerId, currentBuyer);
                                System.out.println("Buyer account created with ID: "+buyerId);
                                System.out.println("Now logged-in as buyer: "+buyerId);
                                break;
                            case "list":
                                for (Buyer buyer : buyers.values()) {
                                    System.out.println("ID: "+buyer.getName() + " " + buyer.getEmail() + " " + buyer.getId());
                                }
                                break;
                        }
                        break;
                    case "auction":
                        if (currentBuyer == null) {
                            System.out.println("Please login first");
                            break;
                        }
                        switch(splitted[1].toLowerCase()) {
                            case "bid":
                                if (splitted.length < 4) {
                                    System.out.println("Please bid using the syntax: 'auction bid <amount> <id>'");
                                } else {
                                    // Bid on an auction item (args[2]), create bid with the bid amount (args[1]), pass this as a reference to who the buyer is
                                    stub.bidAuction(new Bid(Double.parseDouble(splitted[2]), currentBuyer, Integer.parseInt(splitted[3])));
                                    currentBuyer.pollServerIfWon(stub, Integer.parseInt(splitted[3])); // Polls server every second on a new thread to check if item is won
                                }
                                break;
                            case "list":
                                HashMap<Integer, AuctionItem> auctionItems = stub.getAuctionItems();
                                for (AuctionItem item : auctionItems.values()) {
                                    System.out.println("ID: " + "'"+item.getId()+"'" + " Name: " + "'"+item.getName()+"'" + " Description: " + "'"+item.getDescription()+"'");
                                }
                                break;
                        }
                        break;
                }
            }
        } catch(Exception e) {
            System.err.println("Client Exception "+e.toString());
            e.printStackTrace();
        }
    }

}
