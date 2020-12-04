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

            // Start and join JGroups cluster, if the first in the group, keep recreating the channel until not
            while(this.startCluster()) {
                channel.close(); // Close the channel and try again
                Thread.sleep(1000); // Block thread for 1 second
            }
            // If we're not the first, then we can connect
            this.channel.connect("AuctionCluster");
            this.channel.setDiscardOwnMessages(true);
            System.out.println("Server Ready");
        } catch (Exception e) {
            System.err.println("Server exception: "+e.toString());
            e.printStackTrace();
        }
    }

    public boolean startCluster() {
        boolean returnVal = true;
        try {
            channel = new JChannel();
            this.requestOptions = new RequestOptions(ResponseMode.GET_ALL, TIMEOUT);
            this.dispatcher = new RpcDispatcher(this.channel, this);
            this.channel.connect("AuctionCluster");
            this.channel.setDiscardOwnMessages(true);
            returnVal = isFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnVal;
    }

    // Returns true if server is the first to join the JChannel view
    public boolean isFirst() {
        boolean returnVal = false;
        View view = channel.getView();
        if (view.getMembers().isEmpty()) {
            return true;
        }
        for (int i = 0; i < view.getMembers().size(); i++) {
            if (i == 0 && channel.getAddress().equals(view.getMembers().get(i))) returnVal = true;
        }
        return returnVal;
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
        return list;
    }

    // Used to find the replica with the most current state, send the state out to all other replicas with their state
    // To fix any state issues
    public void synchroniseReplicas(Map.Entry entry, RspList responses) {
        List<Address> clusterMembers = getReplicas();
        Address addressWithMostVotes = clusterMembers.get(0);
        for (Address address : clusterMembers) {
            if (responses.get(address).getValue() == entry.getKey()) {
                addressWithMostVotes = address;
                break;
            }
        }
        //System.out.println("Replica with most current state "+responses.get(addressWithMostVotes).getValue()+" : "+addressWithMostVotes);

        List<Address> list = new LinkedList<>();
        list.add(addressWithMostVotes);
        try {
            this.dispatcher.callRemoteMethods(
                    list,
                    "synchroniseState",
                    new Object[]{addressWithMostVotes},
                    new Class[]{Address.class},
                    this.requestOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if all the responses are equal
    // If true: return response
    // If false: majority vote
    public <T> T voteOnResponses(RspList responses, HashMap<T, Integer> list) {
        List<Address> clusterMembers = getReplicas();
        T returnVal = (T)responses.get(clusterMembers.get(0)).getValue();
        System.out.println(responses);
        for (Address address : clusterMembers) {
            Rsp response = responses.get(address);
            //System.out.println(address+ ": "+response.getValue());
            if (!response.getValue().equals(responses.get(clusterMembers.get(0)).getValue())) { // Not all the same, return the value with the highest agreement value
                System.out.println("Not all values are the same!");
                int maxValue = Collections.max(list.values()); // Value that is highest in the hashmap
                // Need to find the pairing key to the maxValue
                for (Map.Entry<T, Integer> entry : list.entrySet()) {
                    if (Objects.equals(maxValue, entry.getValue())) {
                        // Synchronise replica state from the most voted state
                        synchroniseReplicas(entry, responses);
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
        LinkedList<Address> list = new LinkedList<>();
        for (Address address : addresses) {
            if (!address.equals(channel.getAddress())) {
                list.add(address);
            }
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
        double returnVal = 0;
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "bidAuction",
                    new Object[]{bid},
                    new Class[]{Bid.class},
                    this.requestOptions);
            HashMap<Double, Integer> list = buildMap(responses);
            returnVal = voteOnResponses(responses, list);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return returnVal;
    }
    // When client requests to close auction
    @Override
    public double closeAuction(int itemId, int clientId) {
        double returnVal = 0;
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                    null,
                    "closeAuction",
                    new Object[]{itemId, clientId},
                    new Class[]{int.class, int.class},
                    this.requestOptions);
            HashMap<Double, Integer> list = buildMap(responses);
            returnVal = voteOnResponses(responses, list);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return returnVal;
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
            HashMap<ArrayList<Integer>, Integer> list = buildMap(responses);
            buyers = voteOnResponses(responses, list);
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
            HashMap<ArrayList<Integer>, Integer> list = buildMap(responses);
            sellers = voteOnResponses(responses, list);
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
            HashMap<AuctionItem, Integer> list = buildMap(responses);
            item = voteOnResponses(responses, list);
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
            HashMap<HashMap<Integer, AuctionItem>, Integer> list = buildMap(responses);
            auctionItems = voteOnResponses(responses, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return auctionItems;
    }
    @Override
    public void addBuyer(int buyerId) {
        try {
            this.dispatcher.callRemoteMethods(
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
            this.dispatcher.callRemoteMethods(
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
            this.dispatcher.callRemoteMethods(
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
            this.dispatcher.callRemoteMethods(
                    null,
                    "removeBuyer",
                    new Object[]{id},
                    new Class[]{int.class},
                    this.requestOptions);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            AuctionFrontend auctionFrontend = new AuctionFrontend();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}