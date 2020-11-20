package com.nathanial.auction;

import javax.crypto.SealedObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.HashMap;

public interface RMIService extends Remote {
    public AuctionItem getSpec(int itemId, int clientId) throws RemoteException;
    public SealedObject getSpec(int itemId, SealedObject clientRequest) throws RemoteException;

    public HashMap<Integer, Buyer> getBuyers() throws RemoteException;
    public HashMap<Integer, Seller> getSellers() throws RemoteException;
    public void addBuyer(Buyer buyer) throws RemoteException;
    public void addSeller(Seller selller) throws RemoteException;
    public HashMap<Integer, AuctionItem> getAuctionItems() throws RemoteException;
    public AuctionItem getAuctionItem(int id) throws RemoteException;
    public int createAuction(Seller seller, double startingPrice, String description, String name, double reserve) throws RemoteException;
    public double bidAuction(Bid bid) throws RemoteException;
    public double closeAuction(int itemId, Client client) throws RemoteException;
    public boolean authoriseBuyer(int clientId) throws RemoteException;
    public boolean authoriseSeller(int clientId) throws RemoteException;
    public byte[] challengeServer(byte[] message) throws RemoteException;
    public PublicKey getPublicKey() throws RemoteException;
    public void removeBuyer(int id) throws RemoteException;
    public void removeSeller(int id) throws RemoteException;
}