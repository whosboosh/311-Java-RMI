package com.nathanial.auction;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class Buyer implements Client {

    public Buyer(String name, String email, Integer id) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.authToken = Base64.getEncoder().encodeToString(generateHash(name+email));
    }
    private String name;
    private String email;
    private Integer id;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String authToken;

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public Integer getId() {
        return id;
    }
    public PublicKey getPublicKey() { return publicKey; }
    public String getAuthToken() { return authToken; }
    public void generateKeys() {
        // Generate public and private keys
        KeyPair keyPair = Utilities.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    private byte[] generateHash(String input) {
        byte[] bytes = new byte[30];
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            bytes = md.digest(input.getBytes()); // Hash the message using SHA-256
        } catch(Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * Challenge the server to encrypt hash using their private key, if we can decrypt it using their public key then we know they're real
     * @param stub stub to RMIService
     */
    public boolean authoriseServer(RMIService stub) {
        boolean authorised = false;
        try {
            byte[] messageHash = Utilities.generateHash(Utilities.generateBytes()); // Generate a SHA-256 hash of a random byte array for challenge
            byte[] serverResponse = stub.challengeServer(messageHash); // Send the hash to the server, they encrypt it using their private key and return
            /* With signature class
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(messageHash);
            boolean isCorrect = signature.verify(serverResponse); // Verifies against message hash that's passed in from signature.update()
            */
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, stub.getPublicKey()); // Decrypt the message with servers public key
            byte[] digitalSignature = cipher.doFinal(serverResponse); // Get the decrypted value
            if (Arrays.equals(digitalSignature, messageHash)) {
                System.out.println("Server is authorised");
                // Now that server is authorised, the server still needs to authorise us.
                // Call to server to authorise client, performs the same thing but in reverse
                byte[] serverHash = stub.generateMessage(id);
                byte[] encryptedHash = Utilities.performChallenge(privateKey, serverHash);
                if (stub.authoriseClient(encryptedHash, publicKey, id)) {
                    System.out.println("Server has authorised you");
                    authorised = true;
                } else {
                    System.out.println("Server failed to authorise you");
                    authorised = false;
                }
            } else {
                System.out.println("Failed to authorise server");
                authorised = false;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return authorised;
    }

    public byte[] challengeClient(byte[] message) {
        return Utilities.performChallenge(privateKey, message);
    }

    // Ping server every second to see if won
    public void pollServerIfWon(RMIService stub, Integer itemId) {
        new Thread( new Runnable() {
            boolean hasSold = false;
            public void run() {
                while (!hasSold) { // As long as the item hasn't been sold, keep checking if won
                    try {
                        AuctionItem item = stub.getAuctionItem(itemId);
                        hasSold = item.isSold();
                        if (hasSold) {
                            if (item.getWinningBuyerId() == null) System.out.println("Reserve was not met. The listing with ID: "+itemId+" has been closed.");
                            else if (item.getWinningBuyerId().equals(id)) System.out.println("You've won! ID: " + item.getId() + " " + item.getName() + " " + item.getDescription());
                            else System.out.println("You've didn't win... ID: " + item.getId() + " " + item.getName() + " " + item.getDescription());
                            break;
                        }
                        Thread.sleep(1000);
                    }
                    catch (Exception e) {
                        System.err.println("Client Exception "+e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}