package com.nathanial.auction;
import com.sun.security.ntlm.Server;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Replica extends ImplRMI {
    //final private ServerData data = new ServerData(); // ServerData encapsulates AuctionItems, Users. It's the state that is maintained between replicas
    private JChannel channel;

    public Replica() {
        System.out.println("Replica ready");
        this.startCluster();
    }

    public void startCluster() {
        try {
            channel = new JChannel();
            channel.setReceiver(this);
            channel.connect("AuctionCluster");
            channel.getState(null, 10000);
            // TODO: How to close channel
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Called when a new member joins cluster
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    /*
    // On receiving a message within the JGroups cluster
    public void receive(Message msg) {
        try {
            System.out.println(msg.getSrc() + ": " + msg.getObject());
            ServerData data = (ServerData) Util.objectFromByteBuffer(msg.getRawBuffer(), msg.getOffset(), msg.getLength());
            synchronized (this.data) {
                this.data.setValues(data.getAuctionItems(), data.getBuyers(), data.getSellers());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    // Called on the state provider, object that already holds existing state. Passed to an output stream to which the state has to be written
    public void getState(OutputStream output) {
        try {
            synchronized (serverData) {
                Util.objectToStream(serverData, new DataOutputStream(output));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Called when member initially joins the cluster. Update the state of the replica with the current data
    public void setState(InputStream input) {
        try {
            ServerData data = (ServerData) Util.objectFromStream(new DataInputStream(input));
            synchronized (serverData) {
                // Initialise the data with the object fetched from stream
                serverData.setValues(data.getAuctionItems(), data.getBuyers(), data.getSellers());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Create an auction
    @Override
    public int createAuction(int sellerId, double startingPrice, String name, String description, double reserve) {
        int result = super.createAuction(sellerId, startingPrice, name, description, reserve);
        System.out.println("Hello, Im createAuction in a replica being run!");
        System.out.println(result);
        return result;
    }
    // Bid on an auction
    @Override
    public synchronized double bidAuction(Bid bid) {
        return super.bidAuction(bid);
    }
    // When client requests to close auction
    @Override
    public double closeAuction(int itemId, int clientId) {
        return super.closeAuction(itemId, clientId);
    }
    @Override
    public void addBuyer(int buyerId) {
        super.addBuyer(buyerId);
    }
    @Override
    public void addSeller(int sellerId) {
        super.addSeller(sellerId);
    }
    @Override
    public void removeSeller(int id) {
        super.removeSeller(id);
    }
    @Override
    public void removeBuyer(int id) {
        super.removeBuyer(id);
    }

    public static void main(String[] args) {
        Replica replica = new Replica();
    }
}
