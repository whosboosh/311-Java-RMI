package com.nathanial.auction;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface JgroupsService extends Remote {
    public ArrayList<Integer> getBuyers() throws RemoteException;
    public ArrayList<Integer> getSellers() throws RemoteException;
    public void addBuyer(int buyerId) throws RemoteException;
    public void addSeller(int selllerId) throws RemoteException;
    public HashMap<Integer, AuctionItem> getAuctionItems() throws RemoteException;
    public AuctionItem getAuctionItem(int id) throws RemoteException;
    public int createAuction(int sellerId, double startingPrice, String description, String name, double reserve) throws RemoteException;
    public double bidAuction(Bid bid) throws RemoteException;
    public double closeAuction(int itemId, int clientId) throws RemoteException;
    public void removeBuyer(int id) throws RemoteException;
    public void removeSeller(int id) throws RemoteException;
}
