package com.nathanial.auction;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

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

    public Integer getId() {return id;}
    public PublicKey getPublicKey() { return publicKey; }

    public boolean authoriseServer(RMIService stub) {
        boolean authorised = false;
        try {
            byte[] messageHash = Utilities.generateHash(Utilities.generateBytes()); // Generate a SHA-256 hash of this string

            byte[] serverResponse = stub.challengeServer(messageHash); // Send the hash to the server, they encrypt it using their private key and return
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, stub.getPublicKey()); // Decrypt the message with servers public key
            byte[] digitalSignature = cipher.doFinal(serverResponse); // Get the decrypted value
            if (Arrays.equals(digitalSignature, messageHash)) {
                System.out.println("Server is authorised");
                // Now that server is authorised, the server still needs to authorise us.
                // Call to server to authorise client, performs the same thing but in reverse
                if (stub.authoriseSeller(id)) {
                    System.out.println("Server has authorised you");
                    authorised = true;
                } else {
                    System.out.println("Server failed to authorise you");
                    authorised = false;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return authorised;
    }

    public byte[] challengeClient(byte[] message) {
        return Utilities.performChallenge(privateKey, message);
    }
}
