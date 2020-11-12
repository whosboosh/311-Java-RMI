import javax.crypto.Cipher;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

public class Seller implements Client {
    public Seller(Integer id) {
        this.id = id;

        // Generate public and private keys
        KeyPair keyPair = Utilities.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

    }
    private Integer id;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private boolean isServerAuthorised = false;

    public Integer getId() {return id;}
    public PublicKey getPublicKey() { return publicKey; }
    public boolean getIsServerAuthorised() { return isServerAuthorised; }

    public void authoriseServer(RMIService stub) {
        try {
            byte[] messageHash = Utilities.generateHash("stringtoverifyserver");

            byte[] serverResponse = stub.challengeServer(messageHash); // Send the hash to the server, they encrypt it using their private key and return
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, stub.getPublicKey()); // Decrypt the message with servers public key
            byte[] digitalSignature = cipher.doFinal(serverResponse); // Get the decrypted value
            if (Arrays.equals(digitalSignature, messageHash)) {
                System.out.println("Server is authorised");
                isServerAuthorised = true;
                // Now that server is authorised, the server still needs to authorise us.
                stub.authoriseClient(this); // Call to server to authorise client, performs the same thing but in reverse
            } else {
                System.out.println("Failed to authorise server");
                isServerAuthorised = false;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] challengeClient(byte[] message) {
        return Utilities.performChallenge(privateKey, message);
    }
}
