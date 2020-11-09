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

            System.out.println("Welcome Buyer!\nPlease use `help` to view available commands");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String[] splitted = input.split("\\s+"); // Split by each word
                if (splitted.length < 2) {
                    System.out.println("Available commands:\nauction bid\nauction list\nauth create\nauth login\nauth show");
                    continue;
                }
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
                            case "show":
                                // Tell user who the curently logged in account is
                                if (currentBuyer == null) {
                                    System.out.println("Not logged in, either login or create an account");
                                    break;
                                }
                                System.out.println("Currently logged in as: "+currentBuyer.getId());
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
                                // Break if they haven't provided enough commands
                                if (splitted.length < 4) {
                                    System.out.println("Please bid using the syntax: 'auction bid <amount> <id>'");
                                    break;
                                }
                                // Bid on an auction item (args[2]), create bid with the bid amount (args[1]), pass this as a reference to who the buyer is
                                if (!stub.bidAuction(new Bid(Double.parseDouble(splitted[2]), currentBuyer, Integer.parseInt(splitted[3])))) {
                                    System.out.println("Bid was not successful. Need to bid more than: "+stub.getAuctionItem(Integer.parseInt(splitted[3])).getHighestBid());
                                    break;
                                };
                                System.out.println("Bid processed successfully");
                                currentBuyer.pollServerIfWon(stub, Integer.parseInt(splitted[3])); // Polls server every second on a new thread to check if item is won
                                break;
                            case "list":
                                HashMap<Integer, AuctionItem> auctionItems = stub.getAuctionItems();
                                if (auctionItems.isEmpty()) {
                                    System.out.println("No auction items are currently available to bid on");
                                    break;
                                }
                                for (AuctionItem item : auctionItems.values()) {
                                    System.out.println("ID: " + "'"+item.getId()+"'" + " Name: " + "'"+item.getName()+"'" + " Description: " + "'"+item.getDescription()+"'" +
                                            " Current Highest Bid: "+item.getHighestBid());
                                }
                                break;
                        }
                        break;
                    case "help":
                        // Need at least 3 parameters for information on commands e.g. `help auction bid`
                        if (splitted.length < 3) {
                            System.out.println("Not enough parameters provided.\nEnter a command such as `help auction bid` to view information about using that command");
                            break;
                        }
                        switch(splitted[1].toLowerCase()) {
                            case "auction":
                                switch(splitted[2].toLowerCase()) {
                                    case "bid":
                                        System.out.println("Bid on an auction item, `auction bid <amount> <id>`");
                                        break;
                                    case "list":
                                        System.out.println("List the current ongoing auctions");
                                        break;
                                }
                                break;
                            case "auth":
                                switch(splitted[2].toLowerCase()) {
                                    case "login":
                                        System.out.println("Login to a user account by providing their ID: `auth login <id>`");
                                        break;
                                    case "create":
                                        System.out.println("Create a user account using the following syntax: `auth create <username> <email>`");
                                        break;
                                    case "show":
                                        System.out.println("Display the id of the currently logged-in user");
                                        break;
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
