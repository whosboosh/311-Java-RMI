import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class ClientSeller {
    public static void main(String[] args) {
        try {
            // Find the registry
            Registry registry = LocateRegistry.getRegistry(1099);
            RMIService stub = (RMIService) registry.lookup("ServerRMI"); // Create a stub based on the location of "ServerRMI" in the registry

            // Create sellers
            HashMap<Integer, Seller> sellers;
            Seller currentSeller = null;

            System.out.println("Welcome Seller!\nPlease use `help` to view available commands");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String[] splitted = input.split("\\s+");
                // Require user to enter more than 1 input
                if (splitted.length < 2) {
                    System.out.println("Available commands:\nauth create\nauth login\nauth show\nauction add\nauction list\nauction close");
                    continue;
                }
                sellers = stub.getSellers(); // Update list of sellers from server
                switch(splitted[0]) {
                    case "auth":
                        switch(splitted[1].toLowerCase()) {
                            case "create":
                                // Create a new seller with unique id
                                Integer sellerId = 0;
                                for (int i = 0; i < sellers.size(); ++i) {
                                    if (sellers.get(i).getId() == i) sellerId++;
                                    else break;
                                }
                                // Add seller to sellers arraylist, use unique id to create new seller
                                Seller testSeller = new Seller(sellerId);
                                if (!testSeller.authoriseServer(stub)){ // Perform 5 stage challenge response between server and client
                                    System.out.println("Failed to authorise Server, please try creating another account");
                                } else {
                                    currentSeller = testSeller;
                                    stub.addSeller(currentSeller);
                                    System.out.println("Seller account created with ID: " + sellerId);
                                    System.out.println("Logged in as " + sellerId);
                                    break;
                                }
                            case "login":
                                if (sellers.isEmpty()) {
                                    System.out.println("No accounts found, create one with 'auth create'");
                                    break;
                                }
                                // Needs 3 commands to login, check if less than 3 and if true break
                                if (splitted.length < 3) {
                                    System.out.println("Please provide an id to login as");
                                    break;
                                } else {
                                    Seller trySeller = sellers.get(Integer.parseInt(splitted[2]));
                                    if (trySeller == null) {
                                        System.out.println("No seller with ID found");
                                        break;
                                    } else {
                                        currentSeller = trySeller;
                                        System.out.println("Now logged in as: " + trySeller.getId());
                                    }
                                }
                                break;
                            case "show":
                                // Tell user who the curently logged in account is
                                if (currentSeller == null) {
                                    System.out.println("Not logged in, either login or create an account");
                                    break;
                                }
                                System.out.println("Currently logged in as: "+currentSeller.getId());
                                break;
                        }
                        break;
                        case "auction":
                            if (currentSeller == null) {
                                System.out.println("Please login first");
                                break;
                            }
                            switch(splitted[1].toLowerCase()) {
                                case "add":
                                    // Create auction for logged-in seller
                                    if (splitted.length < 6) {
                                        System.out.println("Please create an auction using the syntax: `add <startingPrice> <name> <description> <reserve>");
                                    } else {
                                        // splitted[0] and splitted[1] are the words "auction" and "add"
                                        // splitted[2] = starting price
                                        // splitted[3] = name of item
                                        // splitted[4] = description of item
                                        // splitted[5] = reserve price
                                        int itemId = stub.createAuction(sellers.get(currentSeller.getId()), Double.parseDouble(splitted[2]), splitted[3], splitted[4], Double.parseDouble(splitted[5]));
                                        System.out.println("Auction item created with ID: " + itemId);
                                    }
                                    break;
                                case "list":
                                    // List current auctions for the selected seller
                                    HashMap<Integer, AuctionItem> auctionItems = stub.getAuctionItems();
                                    for (AuctionItem item : auctionItems.values()) {
                                        if (item.getSeller().getId().equals(currentSeller.getId())) {
                                            System.out.println("ID: " + "'" + item.getId() + "'" + " Name: " + "'" + item.getName() + "'" + " Description: " + "'" + item.getDescription() + "'");
                                        }
                                    }
                                    break;
                                case "close":
                                    if (splitted.length < 3) {
                                        System.out.println("Please provide 3 parameters to close auction, `auction close <id>`");
                                        break;
                                    }
                                    // Close the auction and flag buyer they've won
                                    double hasClosed = stub.closeAuction(Integer.parseInt(splitted[2]), currentSeller);
                                    AuctionItem item = stub.getAuctionItem(Integer.parseInt(splitted[2]));
                                    if (hasClosed == 0) System.out.println("Auction has closed for "+item.getId()+". The winner was "+stub.getBuyers().get(item.getWinningBuyerId()).getName());
                                    else if (hasClosed == -1) System.out.println("Item ID provided was not valid, use `auction list` to view current auctions");
                                    else if (hasClosed == -2) System.out.println("You are not authorised to close this auction");
                                    else if (hasClosed == -3) System.out.println("There are no bids on the item yet");
                                    else if (hasClosed == -4) System.out.println("Failed to meet reserve price, max bid was: "+item.getHighestBid() + ". Reserve was "+item.getReserve());
                                    break;
                                case "bids":
                                    ArrayList<Bid> bids = stub.getAuctionItems().get(Integer.parseInt(splitted[2])).getCurrentBids();
                                    for (Bid bid : bids) {
                                        System.out.println("Amount: "+bid.getBidAmount() + " Buyer: "+bid.getBuyer() + " For item: " + bid.getItemId());
                                    }
                                    break;
                            }
                            break;
                    case "help":
                        // Need at least 3 parameters for information on commands e.g. `help auction create`
                        if (splitted.length < 3) {
                            System.out.println("Not enough parameters provided.\nEnter a command such as `help auction create` to view information about using that command");
                            break;
                        }
                        switch(splitted[1].toLowerCase()) {
                            case "auction":
                                switch(splitted[2].toLowerCase()) {
                                    case "add":
                                        System.out.println("Bid on an auction item, `auction add <starting-price> <name> <description> <reserve-price>`");
                                        break;
                                    case "list":
                                        System.out.println("List the current ongoing auctions for your seller account");
                                        break;
                                    case "close":
                                        System.out.println("Close an auction, sold to highest bidder. `auction close <id>`");
                                        break;
                                }
                                break;
                            case "auth":
                                switch(splitted[2].toLowerCase()) {
                                    case "login":
                                        System.out.println("Login to a user account by providing their ID: `auth login <id>`");
                                        break;
                                    case "create":
                                        System.out.println("Create a user account using the following syntax: `auth create`");
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

        } catch (Exception e) {
            System.err.println("Client Exception "+e.toString());
            e.printStackTrace();
        }
    }

    /*
    // Part 1 and 2 code for retreiving an auction item
    public void getItem(RMIService stub, Integer itemId) {
        try {
            // Create a cipher using the AES encryption method
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipher.ENCRYPT_MODE, Utilities.getKey());

            // Create a "SealedObject" containing the clientId. Not in use for part 2
            SealedObject sealedRequest = new SealedObject(new ClientRequest(1), cipher);
            // Will generate a SealedResponse on the server encapsulating the auctionItem for the provided itemId.
            SealedObject sealedResponse = stub.getSpec(itemId, sealedRequest);

            // Set the cipher mode to decrypt, decrypt the message sent from the server
            cipher.init(cipher.DECRYPT_MODE, Utilities.getKey());
            AuctionItem auctionItem = (AuctionItem) sealedResponse.getObject(cipher);
            System.out.println("ID: " + "'"+auctionItem.getId()+"'" + " Name: " + "'"+auctionItem.getName()+"'" + " Description: " + "'"+auctionItem.getDescription()+"'");
        } catch(Exception e) {
            System.err.println("Client Exception "+e.toString());
            e.printStackTrace();
        }
    }*/
}
