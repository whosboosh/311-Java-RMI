package com.nathanial.auction;
import org.jgroups.*;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Replica extends AuctionImpl implements JgroupsService  {
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
                HashMap<Integer, AuctionItem> auctionItems = new HashMap<>();
                auctionItems.put(0, new AuctionItem("asdas", "asdas", 50.0, 0, 0, 0));
                Message msg=new Message(null);
                msg.setBuffer(Util.objectToByteBuffer(new ServerData(auctionItems, super.getBuyers(), super.sellers)));
                channel.send(msg);
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
    @Override
    public int createAuction(int sellerId, double startingPrice, String name, String description, double reserve) {
        int result =  super.createAuction(sellerId, startingPrice, name, description, reserve);
        synchroniseState();
        return result;
    }
    // Bid on an auction
    @Override
    public synchronized double bidAuction(Bid bid) {
        double result = super.bidAuction(bid);
        synchroniseState();
        return result;
    }
    // When client requests to close auction
    @Override
    public double closeAuction(int itemId, int clientId) {
        double result =  super.closeAuction(itemId, clientId);
        synchroniseState();
        return result;
    }
    @Override
    public void addBuyer(int buyerId) {
        super.addBuyer(buyerId);
        synchroniseState();
    }
    @Override
    public void addSeller(int sellerId) {
        super.addSeller(sellerId);
        synchroniseState();
    }
    @Override
    public void removeSeller(int id) {
        super.removeSeller(id);
        synchroniseState();
    }
    @Override
    public void removeBuyer(int id) {
        super.removeBuyer(id);
        synchroniseState();
    }
    @Override
    public ArrayList<Integer> getBuyers() {
        return super.getBuyers();
    }
    @Override
    public ArrayList<Integer> getSellers() {
        return super.getSellers();
    }
    @Override
    public HashMap<Integer, AuctionItem> getAuctionItems() {
        return super.getAuctionItems();
    }
    @Override
    public AuctionItem getAuctionItem(int id) {
        return super.getAuctionItem(id);
    }

    public static void main(String[] args) throws Exception {
        new Replica().startCluster();
    }
}
