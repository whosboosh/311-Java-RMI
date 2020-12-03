package com.nathanial.auction;

import javax.crypto.SealedObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public interface RMIService extends Remote {
    public AuctionItem getSpec(int itemId, int clientId) throws RemoteException;
    public SealedObject getSpec(int itemId, SealedObject clientRequest) throws RemoteException;

    public ArrayList<Integer> getBuyers() throws RemoteException;
    public ArrayList<Integer> getSellers() throws RemoteException;
    public void addBuyer(int buyerId) throws RemoteException;
    public void addSeller(int selllerId) throws RemoteException;
    public HashMap<Integer, AuctionItem> getAuctionItems() throws RemoteException;
    public AuctionItem getAuctionItem(int id) throws RemoteException;
    public int createAuction(int sellerId, double startingPrice, String description, String name, double reserve) throws RemoteException;
    public double bidAuction(Bid bid) throws RemoteException;
    public double closeAuction(int itemId, int clientId) throws RemoteException;
    public boolean authoriseClient(byte[] encryptedHash, PublicKey publicKey, int clientId, String type) throws RemoteException;
    public byte[] challengeServer(byte[] message) throws RemoteException;
    public PublicKey getPublicKey() throws RemoteException;
    public void removeBuyer(int id) throws RemoteException;
    public void removeSeller(int id) throws RemoteException;
    public byte[] generateMessage(int clientId) throws RemoteException;
}