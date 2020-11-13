package com.nathanial.auction;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.Random;

public class Utilities {
    // Generate a keypair based on RSA algorithm with key size of 2048 bits. Used for asymmetric authentication
    public static KeyPair generateKeyPair() {
        KeyPair keyPair = null;
        try {
            SecureRandom secureRandom = new SecureRandom();
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048, secureRandom);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch(Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
        return keyPair;
    }

    public static void saveKey(byte[] encodedKey, String name) {
        try {
            Files.createDirectories(Paths.get("keys"));
            Path path = Paths.get("keys/"+name+".key");
            Files.write(path, encodedKey);
            System.out.println("Written key to file");
        } catch(IOException e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
    }

    // Generate an encrypted message with private key and return response
    public static byte[] performChallenge(PrivateKey privateKey, byte[] message) {
        byte[] response = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            response = cipher.doFinal(message);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static byte[] generateHash(byte[] input) {
        byte[] returnHash = new byte[30];
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            returnHash = md.digest(input); // Hash the message using SHA-256
        } catch(Exception e) {
            e.printStackTrace();
        }
        return returnHash;
    }

    // Generate random bytes
    public static byte[] generateBytes() {
        Random random = new Random();
        byte[] bytes = new byte[30];
        random.nextBytes(bytes);
        return bytes;
    }

    public static void generateKey() {
        try {
            // Generate a secret key based on randomness provided by SecureRandom. Using AES cryptography
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init(secureRandom);
            SecretKey key = keyGenerator.generateKey();
            saveKey(key); // Save the key to a text file so both client and server have access to the same key
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
    }

    public static void saveKey(SecretKey key) {
        try {
            byte[] encoded = key.getEncoded(); // Turn key into a byte array for saving to text file
            Path path = Paths.get("key.txt");
            Files.write(path, encoded);
            System.out.println("Written key to file");
        } catch(IOException e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
    }

    public static Key getKey(String path) {
        SecretKey key = null;
        try {
            byte[] encoded = Files.readAllBytes(Paths.get("Keys/"+path));
            key = new SecretKeySpec(encoded, "AES"); // Generate key from byte array

        } catch(Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
        return key;
    }
}
