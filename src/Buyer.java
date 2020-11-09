import javax.management.remote.rmi.RMIServer;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Buyer implements Serializable {

    public Buyer(String name, String email, Integer id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }
    private String name;
    private String email;
    private Integer id;

    private Boolean shouldPoll = true;

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public Integer getId() {
        return id;
    }

    public void pollServerIfWon(RMIService stub, Integer itemId) {
            new Thread( new Runnable() {
                public void run() {
                    while (shouldPoll) {
                        try {
                            shouldPoll = !stub.indicateWinner(itemId, getId()); // If shouldPoll is negative then they've won, indicateWinner returns true if won so we flip
                            if (!shouldPoll) {
                                AuctionItem wonItem = stub.getClosedAuctionItems().get(itemId);
                                System.out.println("You've won! ID: " + wonItem.getId() + " " + wonItem.getName() + " " + wonItem.getDescription());
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
