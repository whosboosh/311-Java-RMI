package com.nathanial.auction;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FrontEndServer extends ImplRMI {
    private JChannel channel;
    private RpcDispatcher dispatcher;
    private RequestOptions requestOptions;
    private static final int TIMEOUT = 1000;

    public FrontEndServer() {
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
            this.dispatcher = new RpcDispatcher(this.channel, new Replica());
            this.channel.connect("AuctionCluster");
            this.channel.setDiscardOwnMessages(true);
            // TODO: How to close channel
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void sendMessageToReplicas() {
        System.out.println("Auction added, Sending messages to all replicas in cluster");
        try {
            Message message = new Message(null);
            message.setBuffer(Util.objectToByteBuffer(new ServerData(auctionItems, buyers, sellers)));
            channel.send(message);
            System.out.println("Sent message to channel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create an auction
    @Override
    public int createAuction(int sellerId, double startingPrice, String name, String description, double reserve) {
        List<Address> clusterMembers = getReplicas();
        System.out.println(Arrays.toString(clusterMembers.toArray()));
        try {
            RspList responses = this.dispatcher.callRemoteMethods(
                                                                    clusterMembers,
																	"createAuction",
																	new Object[]{sellerId, startingPrice, name, description, reserve},
																	new Class[]{int.class, double.class, String.class, String.class, double.class},
																	this.requestOptions);
            System.out.println("Responses: " + responses);
            /*
            for (Address address : clusterMembers) {
                Rsp response = responses.get(address);
                System.out.println(response.getValue());
            }*/
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    // Bid on an auction
    @Override
    public synchronized double bidAuction(Bid bid) {
        return 0;
    }
    // When client requests to close auction
    @Override
    public double closeAuction(int itemId, int clientId) {
        return 0;
    }
    @Override
    public void addBuyer(int buyerId) {

    }
    @Override
    public void addSeller(int sellerId) {

    }
    @Override
    public void removeSeller(int id) {

    }
    @Override
    public void removeBuyer(int id) {

    }

    public static void main(String[] args) {
        try {
            FrontEndServer frontEndServer = new FrontEndServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}