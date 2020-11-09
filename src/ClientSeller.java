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
            HashMap<Integer, Seller> sellers = new HashMap<>();
            Seller currentSeller = null;

            System.out.println("auth create\nauth login\nauth list\nauction add\nauction list\nauction close\n");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String[] splitted = input.split("\\s+");
                System.out.println(Arrays.toString(splitted));
                switch(splitted[0]) {
                    case "auth":
                        switch(splitted[1].toLowerCase()) {
                            case "list":
                                // List current seller accounts
                                for (Seller seller : sellers.values()) {
                                    System.out.println("ID: " + seller.getId());
                                }
                                break;
                            case "create":
                                // Create a new seller with unique id
                                Integer sellerId = 0;
                                for (int i = 0; i < sellers.size(); ++i) {
                                    if (sellers.get(i).getId() == i) sellerId++;
                                    else break;
                                }
                                // Add seller to sellers arraylist, use unique id to create new seller
                                currentSeller = new Seller(sellerId);
                                sellers.put(sellerId, currentSeller);
                                System.out.println("Seller account created with ID: " + sellerId);
                                System.out.println("Logged in as " + sellerId);
                                break;
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
                                        Integer itemId = stub.createAuction(sellers.get(currentSeller.getId()), Double.parseDouble(splitted[2]), splitted[3], splitted[4], Double.parseDouble(splitted[5]));
                                        System.out.println("Auction item created with ID" + itemId);
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
                                    // Close the auction and flag buyer they've won
                                    stub.closeAuction(Integer.parseInt(splitted[2]));
                                    break;
                                case "bids":
                                    ArrayList<Bid> bids = stub.getAuctionItems().get(Integer.parseInt(splitted[2])).getCurrentBids();
                                    for (Bid bid : bids) {
                                        System.out.println("Amount: "+bid.getBidAmount() + " Buyer: "+bid.getBuyer() + " For item: " + bid.getItemId());
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
    }
}
