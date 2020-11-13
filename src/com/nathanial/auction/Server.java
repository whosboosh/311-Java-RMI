package com.nathanial.auction;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server extends ImplRMI {
    public Server(){
        try {
            ImplRMI server = new ImplRMI(); // Create instance of the implementation of remote interface
            server.generateKeys();

            // Cast this object back to the interface
            // Creates a "stub" which is a gateway to the object for the client
            RMIService stub = (RMIService) UnicastRemoteObject.exportObject(server, 1099);
            Registry registry = LocateRegistry.createRegistry(1099); // Start registry
            registry.rebind("ServerRMI", stub); // Bind stub to registry on "ServerRMI"

            System.out.println("Server Ready");
        } catch (RemoteException e) {
            System.err.println("Server exception: "+e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}