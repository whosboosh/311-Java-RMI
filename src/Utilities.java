import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.SecureRandom;

public class Utilities {
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

    public static Key getKey() {
        SecretKey key = null;
        try {
            byte[] encoded = Files.readAllBytes(Paths.get("key.txt"));
            key = new SecretKeySpec(encoded, "AES"); // Generate key from byte array

        } catch(Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
        return key;
    }
}
