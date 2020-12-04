package com.nathanial.auction;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class AuctionFrontend extends AuctionImpl {
    private JChannel channel;
    private RpcDispatcher dispatcher;
    private RequestOptions requestOptions;
    private static final int TIMEOUT = 1000;

    public AuctionFrontend() {
        try {
            // Cast this object back to the interface
            // Creates a "stub" which is a gateway to the object for the client
            RMIService stub = (RMIService) UnicastRemoteObject.exportObject(this, 1099);
            Registry registry = LocateRegistry.createRegistry(1099); // Start registry
            registry.rebind("ServerRMI", stub); // Bind stub to registry on "ServerRMI"

            this.generateKeys(); // Generate asymmetric keys
            this.startCluster(); // Start and join JGroups cluster
            System.out.println("Server Ready");
        } catch (Exception e) {
            System.err.println("Server exception: "+e.toString());
            e.printStackTrace();
        }
    }

    public void startCluster() {
        try {
            channel = new JChannel();
            this.requestOptions = new RequestOptions(ResponseMode.GET_ALL, TIMEOUT);
            this.dispatcher = new RpcDispatcher(this.channel, this);
            this.channel.connect("AuctionCluster");
            this.channel.setDiscardOwnMessages(true);
            // TODO: How to close channel
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Generates a hashmap based on replica response. Builds key/pair = (value returned, number of times it was sent from a replica)
    public <T> HashMap<T, Integer> buildMap(RspList responses) {
        HashMap<T, Integer> list = new HashMap<>();
        List<Address> clusterMembers = getReplicas();
        for (Address address : clusterMembers) {
            Rsp response = responses.get(address);
            T value = (T)response.getValue();
            int count = list.getOrDefault(value, 0);
            list.put(value, count+1);
        }
        System.out.println(Arrays.toString(list.keySet().toArray()));
        System.out.println(Arrays.toString(list.values().toArray()));
        return list;
    }

    // Check if all the responses are equal
    // If true: return response
    // If false: majority vote
    public <T> T voteOnResponses(RspList responses, HashMap<T, Integer> list) {
        List<Address> clusterMembers = getReplicas();
        T returnVal = (T)responses.get(clusterMembers.get(0)).getValue();
        for (Address address : clusterMembers) {
            Rsp response = responses.get(address);
            System.out.println(address+ ": "+response.getValue());
            if (!response.equals(responses.get(clusterMembers.get(0)))) { // Not all the same, return the value with the highest agreement value
                System.out.println("Not all values are the same!");
                int maxValue = Collections.max(list.values()); // Value that is highest in the hashmap
                // Need to find the pairing key to the maxValue
                for (Map.Entry<T, Integer> entry : list.entrySet()) {
                    if (Objects.equals(maxValue, entry.getValue())) {
                        returnVal = entry.getKey();
                        break;
                    }
                }
            }
        }
        return returnVal;
    }

    // Return array of addresses for all other members in cluster excluding the server
    public List<Address> getReplicas() {
        View view = channel.getView();
        List<Address> addresses = view.getMembers();
        // Create linkedlist of addresses
        LinkedList<Address> list = new LinkedList<>(addresses);
        int i = 0;
        for (Address address : list) {
            if (address == channel.getAddress()) {
                list.remove(i);
                break;
            }
            i++;
        }
        return list;
    }

    // Create an auction
    @Override
    public int createAuction(int sellerId, double startingPrice, String name, String description, double reserve) {
        int returnVal = 0;
        //System.out.println(Arrays.toString(clusterMembers.toArray()));
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                                                                    null,
																	"createAuction",
																	new Object[]{sellerId, startingPrice, name, description, reserve},
																	new Class[]{int.class, double.class, String.class, String.class, double.class},
																	this.requestOptions);

             if (responses.getResults().isEmpty()) {
                 throw new Error("No valid responses from replicas found");
             }
            HashMap<Integer, Integer> list = buildMap(responses);
            returnVal = voteOnResponses(responses, list);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return returnVal; // ID of the new auction item
    }
    // Bid on an auction
    @Override
    public synchronized double bidAuction(Bid bid) {
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "bidAuction",
                    new Object[]{bid},
                    new Class[]{Bid.class},
                    this.requestOptions);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    // When client requests to close auction
    @Override
    public double closeAuction(int itemId, int clientId) {
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "closeAuction",
                    new Object[]{itemId, clientId},
                    new Class[]{int.class, int.class},
                    this.requestOptions);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    @Override
    public void addBuyer(int buyerId) {
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "addBuyer",
                    new Object[]{buyerId},
                    new Class[]{int.class},
                    this.requestOptions);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void addSeller(int sellerId) {
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "addSeller",
                    new Object[]{sellerId},
                    new Class[]{int.class},
                    this.requestOptions);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void removeSeller(int id) {
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "removeSeller",
                    new Object[]{id},
                    new Class[]{int.class},
                    this.requestOptions);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void removeBuyer(int id) {
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "removeBuyer",
                    new Object[]{id},
                    new Class[]{int.class},
                    this.requestOptions);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<Integer> getBuyers() {
        ArrayList<Integer> buyers = new ArrayList<>();
        try {
            List<Address> clusterMembers = getReplicas();
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "getBuyers",
                    null,
                    null,
                    this.requestOptions);
        for (Address address : clusterMembers) {
            buyers = (ArrayList<Integer>)responses.get(address).getValue();
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buyers;
    }

    @Override
    public ArrayList<Integer> getSellers() {
        ArrayList<Integer> sellers = new ArrayList<>();
        try {
            List<Address> clusterMembers = getReplicas();
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "getSellers",
                    null,
                    null,
                    this.requestOptions);
            for (Address address : clusterMembers) {
                buyers = (ArrayList<Integer>)responses.get(address).getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sellers;
    }
    @Override
    public AuctionItem getAuctionItem(int id) {
        AuctionItem item = null;
        try {
            List<Address> clusterMembers = getReplicas();
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "getAuctionItem",
                    new Object[]{id},
                    new Class[]{int.class},
                    this.requestOptions);
            for (Address address : clusterMembers) {
                item = (AuctionItem)responses.get(address).getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }
    @Override
    public HashMap<Integer, AuctionItem> getAuctionItems() {
        HashMap<Integer, AuctionItem> auctionItems = new HashMap<>();
        try {
            List<Address> clusterMembers = getReplicas();
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "getAuctionItems",
                    null,
                    null,
                    this.requestOptions);
            for (Address address : clusterMembers) {
                auctionItems = (HashMap<Integer, AuctionItem>) responses.get(address).getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return auctionItems;
    }


    public static void main(String[] args) {
        try {
            AuctionFrontend auctionFrontend = new AuctionFrontend();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}