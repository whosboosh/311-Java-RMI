package com.nathanial.auction;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;

public class ClientBuyer {
    public static void main(String[] args) {
        ClientBuyer clientBuyer = new ClientBuyer();
        clientBuyer.startInput();
    }

    public ClientBuyer() {
        try {
            // Find the registry
            Registry registry = LocateRegistry.getRegistry();
            stub = (RMIService) registry.lookup("ServerRMI"); // Create a stub based on the location of "ServerRMI" in the registry
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<Integer, Buyer> buyers;
    private Buyer currentBuyer = null;
    private RMIService stub;

    private void startInput() {
        try {
            System.out.println("Welcome Buyer!\nPlease use `help` to view available commands");
            Scanner scanner = new Scanner(System.in);

            // Loop user input
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String[] splitted = input.split("\\s+"); // Split by each word
                if (splitted.length < 2) {
                    System.out.println("Available commands:\nauction bid\nauction list\nauth create\nauth login\nauth show");
                    continue;
                }
                this.buyers = stub.getBuyers(); // Update list of buyers from server
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
                                    System.out.println("Please provide a token to login as");
                                    break;
                                } else {
                                    boolean found = false;
                                    for (Buyer buyer: buyers.values()) {
                                        if (buyer.getAuthToken().equals(splitted[2])) {
                                            currentBuyer = buyer;
                                            System.out.println("Now logged in as: " + buyer.getName() +" "+ buyer.getEmail() +" "+buyer.getId());
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) System.out.println("No buyer with token found");
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
                                Buyer testBuyer = new Buyer(splitted[2], splitted[3], buyerId);
                                // Perform 5 stage challenge response between server and client
                                if (!testBuyer.authoriseServer(stub)){
                                    // If failed authorisation, remove the buyer from the list
                                    System.out.println("Failed to authorise Server, please try creating another account");
                                } else {
                                    currentBuyer = testBuyer;
                                    stub.addBuyer(currentBuyer);
                                    System.out.println("Buyer account created with ID: "+buyerId+" And token: "+currentBuyer.getAuthToken());
                                    System.out.println("Now logged-in as buyer: "+currentBuyer.getId()+","+currentBuyer.getName()+","+currentBuyer.getEmail());
                                }
                                break;
                            case "show":
                                // Tell user who the currently logged in account is
                                if (currentBuyer == null) {
                                    System.out.println("Not logged in, either login or create an account");
                                    break;
                                }
                                System.out.println("Currently logged in as: " + currentBuyer.getName() +" "+ currentBuyer.getEmail() +" "+currentBuyer.getId());
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
                                Integer id = Integer.parseInt(splitted[3]);
                                // Bid on an auction item (args[2]), create bid with the bid amount (args[1]), pass this as a reference to who the buyer is
                                double result = stub.bidAuction(new Bid(Double.parseDouble(splitted[2]), currentBuyer, id));
                                if (result == -1) {
                                    System.out.println("Item ID provided is not valid");
                                    break;
                                }
                                else if (result == -2) {
                                    System.out.println("Item has been sold, cannot bid");
                                    break;
                                }
                                else if (result == -3) {
                                    System.out.println("Bid was not successful. Need to bid more than: "+stub.getAuctionItem(id).getHighestBid());
                                    break;
                                }
                                System.out.println("Bid processed successfully");

                                // Check if this buyer has a bid on the item already, if they do return. This is because the pollServerIfWon function only needs to be run once per buyer per item
                                int timesBidded = 0;
                                ArrayList<Bid> currentBids = stub.getAuctionItem(id).getCurrentBids();
                                for (Bid bid : currentBids) {
                                    if (bid.getBuyer().getId().equals(currentBuyer.getId())) {
                                        timesBidded++;
                                    }
                                }
                                if (timesBidded <= 1) currentBuyer.pollServerIfWon(stub, id); // Polls server every second on a new thread to check if item is won
                                break;
                            case "list":
                                HashMap<Integer, AuctionItem> auctionItems = stub.getAuctionItems();
                                if (auctionItems.isEmpty()) {
                                    System.out.println("No auction items are currently available to bid on");
                                    break;
                                }
                                for (AuctionItem item : auctionItems.values()) {
                                    if (item.isSold()) continue; // Don't list item if sold
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
                                        System.out.println("Login to a user account by providing their token: `auth login <token>`");
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
            e.printStackTrace();
        }
    }
}
