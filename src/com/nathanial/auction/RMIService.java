package com.nathanial.auction;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface RMIService extends JgroupsService {
    public boolean authoriseClient(byte[] encryptedHash, PublicKey publicKey, int clientId, String type) throws RemoteException;
    public byte[] challengeServer(byte[] message) throws RemoteException;
    public PublicKey getPublicKey() throws RemoteException;
    public byte[] generateMessage(int clientId) throws RemoteException;
}