package com.nathanial.auction;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Util;

import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Replica extends AuctionImpl {
    final private ServerData serverData = new ServerData(); // ServerData encapsulates AuctionItems, Users. It's the state that is maintained between replicas
    private JChannel channel;

    public Replica() {
        System.out.println("Replica ready");
    }

    public void startCluster() {
        try {
            channel = new JChannel();
            channel.setReceiver(this);
            channel.connect("AuctionCluster");
            channel.getState(null, 10000);
            new RpcDispatcher(channel, this, this, this);
            eventLoop();
            channel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void eventLoop() {
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("> "); System.out.flush();
                String line=in.readLine().toLowerCase();
                if(line.startsWith("data")) {
                    System.out.println(serverData.getAuctionItems().size());
                    System.out.println(Arrays.toString(serverData.getBuyers().toArray()));
                    System.out.println(Arrays.toString(serverData.getSellers().toArray()));
                }
            }
            catch(Exception e) {
            }
        }
    }

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void receive(Message msg) {
        try {
            System.out.println("Received message!");
            ServerData item = (ServerData) Util.objectFromByteBuffer(msg.getRawBuffer(), msg.getOffset(), msg.getLength());
            synchronized (serverData) {
                serverData.setValues(item.getAuctionItems(), item.getBuyers(), item.getSellers());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Provide state for other replicas
    public void getState(OutputStream output) throws Exception {
        System.out.println("Providing state with existing details");
        synchronized (serverData) {
            Util.objectToStream(serverData, new DataOutputStream(output));
        }
    }

    // Ran when new replica is made, initialises state with data from other replica
    public void setState(InputStream input) throws Exception {
        System.out.println("Setting state with data from other replicas!");
        ServerData data = (ServerData) Util.objectFromStream(new DataInputStream(input));
        synchronized (serverData) {
            serverData.setValues(data.getAuctionItems(), data.getBuyers(), data.getSellers());
        }
    }

    public void synchroniseState() {
        try {
            System.out.println("Synchronising state with other replicas, sending message");
            Message msg=new Message(null);
            msg.setBuffer(Util.objectToByteBuffer(new ServerData(super.getAuctionItems(), super.getBuyers(), super.getSellers())));
            channel.send(msg);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Create an auction
    public int runCreateAuction(int sellerId, double startingPrice, String name, String description, double reserve) {
        int result =  super.createAuction(sellerId, startingPrice, name, description, reserve);
        synchroniseState();
        return result;
    }
    // Bid on an auction
    public synchronized double runBidAuction(Bid bid) {
        double result = super.bidAuction(bid);
        synchroniseState();
        return result;
    }
    // When client requests to close auction
    public double runCloseAuction(int itemId, int clientId) {
        double result =  super.closeAuction(itemId, clientId);
        synchroniseState();
        return result;
    }
    public void runAddBuyer(int buyerId) {
        super.addBuyer(buyerId);
        synchroniseState();
    }
    public void runAddSeller(int sellerId) {
        super.addSeller(sellerId);
        synchroniseState();
    }
    public void runRemoveSeller(int id) {
        super.removeSeller(id);
        synchroniseState();
    }
    public void runRemoveBuyer(int id) {
        super.removeBuyer(id);
        synchroniseState();
    }
    public ArrayList<Integer> runGetBuyers() {
        return super.getBuyers();
    }
    public ArrayList<Integer> runGetSellers() {
        return super.getSellers();
    }
    public HashMap<Integer, AuctionItem> runGetAuctionItems() {
        return super.getAuctionItems();
    }
    public AuctionItem runGetAuctionItem(int id) {
        return super.getAuctionItem(id);
    }

    public static void main(String[] args) throws Exception {
        new Replica().startCluster();
    }
}
