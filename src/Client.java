import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;


public class Client {
    private Client() {}
    public static void main(String[] args) {
        try {
            // Find the registry
            Registry registry = LocateRegistry.getRegistry(1099);
            RMIService stub = (RMIService) registry.lookup("ServerRMI"); // Create a stub based on the location of "ServerRMI" in the registry

            // Create a cipher using the AES encryption method
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipher.ENCRYPT_MODE, Utilities.getKey());

            // Create a "SealedObject" containing the clientId. Not in use for part 2
            SealedObject sealedRequest = new SealedObject(new ClientRequest(1), cipher);
            // Will generate a SealedResponse on the server encapsulating the auctionItem for the provided itemId.
            SealedObject sealedResponse = stub.getSpec(Integer.parseInt(args[0]), sealedRequest);

            // Set the cipher mode to decrypt, decrypt the message sent from the server
            cipher.init(cipher.DECRYPT_MODE, Utilities.getKey());
            AuctionItem auctionItem = (AuctionItem) sealedResponse.getObject(cipher);
            System.out.println("ID: " + "'"+auctionItem.getId()+"'" + " Name: " + "'"+auctionItem.getName()+"'" + " Description: " + "'"+auctionItem.getDescription()+"'");
        } catch (Exception e) {
            System.err.println("Client Exception "+e.toString());
            e.printStackTrace();
        }
    }

}
